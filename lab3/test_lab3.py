import datetime as dt
import time
import unittest
from typing import Any, Literal
from xml.etree import ElementTree
from xml.etree.ElementTree import Element

import requests

Method = Literal['GET', 'OPTIONS', 'HEAD', 'POST', 'PUT', 'PATCH', 'DELETE']


class UtilSOAP:
    TIME_FORMAT = '%Y-%m-%dT%H:%M:%S.%f000'

    @staticmethod
    def _dict2xml(d: dict[str, Any]) -> str:
        def fmt(x):
            if isinstance(x, dict):
                return UtilSOAP._dict2xml(x)
            if isinstance(x, dt.datetime):
                return x.strftime(UtilSOAP.TIME_FORMAT)

            return x

        return ''.join(f'<gs:{k}>{fmt(v)}</gs:{k}>' for k, v in d.items())

    @staticmethod
    def request(name: str, args: dict[str, Any]):
        return UtilSOAP.from_body(f"<gs:{name}>{UtilSOAP._dict2xml(args)}</gs:{name}>")

    @staticmethod
    def from_body(body: str):
        return f"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                      xmlns:gs="http://lab3.com/dudnyk">
       <soapenv:Header/>
       <soapenv:Body>
          {body}
       </soapenv:Body>
    </soapenv:Envelope>"""

    @staticmethod
    def get_response(content: str):
        if not content:
            return None

        return ElementTree.fromstring(content).find('SOAP-ENV:Body', {
            'SOAP-ENV': 'http://schemas.xmlsoap.org/soap/envelope/'})[0]

    @staticmethod
    def parse_response_simple(body: Element):
        return {element.tag[element.tag.index('}') + 1:]: element.text for element in body}

    @staticmethod
    def parse_response_list(body: Element):
        for element in body:
            yield UtilSOAP.parse_response_simple(element)

    @staticmethod
    def parse_datetime(s: str):
        return dt.datetime.strptime(s.split('.')[0], '%Y-%m-%dT%H:%M:%S')


class SOAP:
    @staticmethod
    def request_soap_raw(envelope: str):
        return requests.post('http://127.0.0.1:8080/ws', data=envelope, headers={'content-type': 'text/xml'})

    @staticmethod
    def request_soap(name: str, args: dict[str, Any]):
        return UtilSOAP.get_response(SOAP.request_soap_raw(UtilSOAP.request(name, args)).text)

    @staticmethod
    def get_doctors():
        doctors = SOAP.request_soap('getDoctorsRequest', {})

        return {int(d['id']): (d['first_name'], d['last_name']) for d in UtilSOAP.parse_response_list(doctors)}

    @staticmethod
    def put_appointment(doc_id: int, patient: str, time: dt.datetime, duration: int):
        response = UtilSOAP.parse_response_simple(SOAP.request_soap('putAppointmentRequest', {
            'doctor_id': doc_id, 'patient': patient, 'time': {'time': time, 'durationMinutes': duration}
        }))

        if len(response) != 1:
            raise ValueError('bad response')

        try:
            return int(response['id'])
        except KeyError | ValueError as e:
            raise ValueError('bad response') from e

    @staticmethod
    def del_appointment(doc_id: int, appointment_id: int):
        SOAP.request_soap('delAppointmentRequest', {'doctor_id': doc_id, 'appointment_id': appointment_id})

    @staticmethod
    def get_windows(doc_id: int, start: dt.datetime, stop: dt.datetime):
        return [*UtilSOAP.parse_response_list(SOAP.request_soap('getWindowsRequest', {'doctor_id': doc_id, 'start': start, 'stop': stop}))]


class REST:
    @staticmethod
    def _encode_datetime(d):
        return {k: REST._encode_datetime(v) if isinstance(v, dict) else (v.strftime(UtilSOAP.TIME_FORMAT) if isinstance(v, dt.datetime) else v) for k, v in d.items()}

    @staticmethod
    def request(method: Method, path: str, **json):
        r = requests.request(method, f'http://127.0.0.1:8080/{path}', json=REST._encode_datetime(json))

        return r.json() if r.text else None

    @staticmethod
    def parse_datetime(s: str):
        return UtilSOAP.parse_datetime(s).replace(tzinfo=dt.timezone.utc).astimezone(None).replace(tzinfo=None)

    @staticmethod
    def get(path: str, **json):
        return REST.request('GET', path, **json)

    @staticmethod
    def put(path: str, **json):
        return REST.request('PUT', path, **json)

    @staticmethod
    def delete(path: str, **json):
        return REST.request('DELETE', path, **json)

    @staticmethod
    def get_doctors():
        doctors = REST.get('doc')

        return {int(d['id']): (d['firstName'], d['lastName']) for d in doctors['doctors']}

    @staticmethod
    def put_appointment(doc_id: int, patient: str, time: dt.datetime, duration: int):
        return REST.put('appointment', doctorId=doc_id, patient=patient, time={'time': time, 'durationMinutes': duration})['id']

    @staticmethod
    def del_appointment(doc_id: int, appointment_id: int):
        REST.delete('appointment', doctorId=doc_id, appointmentId=appointment_id)

    @staticmethod
    def get_windows(doc_id: int, start: dt.datetime, stop: dt.datetime):
        return REST.get('windows', doctorId=doc_id, start=start, stop=stop)['windows']


class MyTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.doctors = SOAP.get_doctors()

        self.assertEqual(self.doctors, REST.get_doctors())

    def soap_check_if_doctor_has_appointments_on_day(self, doc_id: int, day: dt.date):
        t1 = dt.datetime.combine(day, dt.time(0))
        t2 = dt.datetime.combine(day, dt.time(23, 59, 59, 999_999))

        windows = SOAP.get_windows(doc_id, t1, t2)

        if len(windows) != 1:
            return True

        match windows:
            case [{'time': time, 'durationMinutes': dur, **rest}]:
                self.assertFalse(rest)

                return not (t1 == UtilSOAP.parse_datetime(time) and 24 * 60 == int(dur))
            case _:
                self.fail(f'windows do not match pattern ({windows=})')

    def rest_check_if_doctor_has_appointments_on_day(self, doc_id: int, day: dt.date):
        t1 = dt.datetime.combine(day, dt.time(0))
        t2 = dt.datetime.combine(day, dt.time(23, 59, 59, 999_999))

        windows = REST.get_windows(doc_id, t1, t2)

        if len(windows) != 1:
            return True

        match windows:
            case [{'time': time, 'durationMinutes': dur, **rest}]:
                self.assertFalse(rest)

                return not (t1 == REST.parse_datetime(time) and 24 * 60 == int(dur))
            case _:
                self.fail(f'windows do not match pattern ({windows=})')

    def test_soap_response_time(self):
        t0 = time.time()
        SOAP.get_doctors()
        t1 = time.time()
        self.assertLessEqual(t1 - t0, 1)

    def test_rest_response_time(self):
        t0 = time.time()
        REST.get_doctors()
        t1 = time.time()
        self.assertLessEqual(t1 - t0, 1)

    def test_soap_empty_schedule_full_day(self):
        for doc_id in self.doctors:
            self.assertFalse(self.soap_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

    def test_rest_empty_schedule_full_day(self):
        for doc_id in self.doctors:
            self.assertFalse(self.rest_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

    def test_soap_empty_schedule(self):
        for doc_id in self.doctors:
            time_start = dt.datetime.combine(dt.date.today(), dt.time(1))
            time_stop = dt.datetime.combine(time_start.date(), dt.time(23))

            windows = SOAP.get_windows(doc_id, time_start, time_stop)

            match windows:
                case [{'time': time, 'durationMinutes': dur, **rest}]:
                    self.assertFalse(rest)

                    self.assertEqual(time_start, UtilSOAP.parse_datetime(time))
                    self.assertEqual((time_stop - time_start).seconds // 60, int(dur))
                case _:
                    self.fail(f'windows do not match pattern ({windows=})')

    def test_rest_empty_schedule(self):
        for doc_id in self.doctors:
            time_start = dt.datetime.combine(dt.date.today(), dt.time(1))
            time_stop = dt.datetime.combine(time_start.date(), dt.time(23))

            windows = REST.get_windows(doc_id, time_start, time_stop)

            match windows:
                case [{'time': time, 'durationMinutes': dur, **rest}]:
                    self.assertFalse(rest)

                    self.assertEqual(time_start, REST.parse_datetime(time))
                    self.assertEqual((time_stop - time_start).seconds // 60, int(dur))
                case _:
                    self.fail(f'windows do not match pattern ({windows=})')

    def test_soap_appointment_del(self):
        for doc_id in self.doctors:
            t1 = dt.datetime.combine(dt.date.today(), dt.time(13))

            appointment_id = SOAP.put_appointment(doc_id, 'Tom the Patient', t1, 42)

            self.assertTrue(self.soap_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

            SOAP.del_appointment(doc_id, appointment_id)

            self.assertFalse(self.soap_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

    def test_rest_appointment_del(self):
        for doc_id in self.doctors:
            t1 = dt.datetime.combine(dt.date.today(), dt.time(13))

            appointment_id = REST.put_appointment(doc_id, 'Tom the Patient', t1, 42)

            self.assertTrue(self.rest_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

            REST.del_appointment(doc_id, appointment_id)

            self.assertFalse(self.rest_check_if_doctor_has_appointments_on_day(doc_id, dt.date.today()))

    def test_soap_windows_split(self):
        for doc_id in self.doctors:
            t1 = dt.datetime.combine(dt.date.today(), dt.time(1))
            t2 = dt.datetime.combine(t1.date(), dt.time(13))
            t3 = dt.datetime.combine(t1.date(), dt.time(23))

            dur = 42

            appointment_id = SOAP.put_appointment(doc_id, 'John the Patient', t2, dur)

            windows = SOAP.get_windows(doc_id, t1, t3)

            SOAP.del_appointment(doc_id, appointment_id)

            match windows:
                case [{'time': t11, 'durationMinutes': dur1, **rest1}, {'time': t12, 'durationMinutes': dur2, **rest2}]:
                    self.assertFalse(rest1)
                    self.assertFalse(rest2)

                    self.assertEqual(t1, UtilSOAP.parse_datetime(t11))
                    self.assertEqual(t2, UtilSOAP.parse_datetime(t11) + dt.timedelta(minutes=int(dur1)))

                    self.assertEqual(t2 + dt.timedelta(minutes=int(dur)), UtilSOAP.parse_datetime(t12))
                    self.assertEqual(t3, UtilSOAP.parse_datetime(t12) + dt.timedelta(minutes=int(dur2)))
                case _:
                    self.fail(f'windows do not match pattern ({windows=})')

    def test_rest_windows_split(self):
        for doc_id in self.doctors:
            t1 = dt.datetime.combine(dt.date.today(), dt.time(1))
            t2 = dt.datetime.combine(t1.date(), dt.time(13))
            t3 = dt.datetime.combine(t1.date(), dt.time(23))

            dur = 42

            appointment_id = REST.put_appointment(doc_id, 'John the Patient', t2, dur)

            windows = REST.get_windows(doc_id, t1, t3)

            REST.del_appointment(doc_id, appointment_id)

            match windows:
                case [{'time': t11, 'durationMinutes': dur1, **rest1}, {'time': t12, 'durationMinutes': dur2, **rest2}]:
                    self.assertFalse(rest1)
                    self.assertFalse(rest2)

                    self.assertEqual(t1, REST.parse_datetime(t11))
                    self.assertEqual(t2, REST.parse_datetime(t11) + dt.timedelta(minutes=int(dur1)))

                    self.assertEqual(t2 + dt.timedelta(minutes=int(dur)), REST.parse_datetime(t12))
                    self.assertEqual(t3, REST.parse_datetime(t12) + dt.timedelta(minutes=int(dur2)))
                case _:
                    self.fail(f'windows do not match pattern ({windows=})')


if __name__ == '__main__':
    unittest.main()
