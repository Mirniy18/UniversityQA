package com.cs41.dudnyk.lab34;

import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.lab3.dudnyk.Appointment;
import com.lab3.dudnyk.AppointmentTime;
import com.lab3.dudnyk.Doctor;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;

@Component
public class DocRepository {
	private static final Map<Integer, Doctor> doctors = new HashMap<>();
	private static final Map<Integer, List<Appointment>> appointments = new HashMap<>();

	private static int doctorID = 0;
	private static int appointmentID = 0;

	@PostConstruct
	public void initData() {
		newDoc("Bill", "Licenced");
		newDoc("Tom", "Bonded");
		newDoc("Bob", "Insured");
		newDoc("Emmanuel", "Novak");
	}

	private Doctor newDoc(String firstName, String lastNme) {
		var d = new Doctor();
		d.setId(doctorID++);
		d.setFirstName(firstName);
		d.setLastName(lastNme);
		doctors.put(d.getId(), d);
		return d;
	}

//	public Appointment findAppointment(int id) {
//		return appointments.get(id);
//	}

	public List<Doctor> getDoctors() {
		return doctors.values().stream().toList();
	}

	public Appointment addAppointment(int docID, String patient, AppointmentTime date) {
		if (!isFree(docID, new AppointmentTimeWrapper(date))) {
			throw new IllegalArgumentException();
		}

		var a = new Appointment();
		a.setId(appointmentID++);
		a.setPatient(patient);
		a.setDate(date);

		if (appointments.containsKey(docID)) {
			appointments.get(docID).add(a);
		} else {
			var as = new ArrayList<Appointment>();
			as.add(a);
			appointments.put(docID, as);
		}

		return a;
	}

	public boolean removeAppointment(int doctorID, int appointmentID) {
		if (!appointments.containsKey(doctorID)) {
			return false;
		}

		return appointments.get(doctorID).removeIf(a -> a.getId() == appointmentID);
	}

	public boolean isFree(int docID, AppointmentTimeWrapper date) {
		var t = date.time();

		var windows = findWindowsWrapped(docID, t, t.plusMinutes(date.durationMinutes()));

		for (AppointmentTimeWrapper window : windows) {
			if (!window.time().isAfter(t) && !t.isAfter(window.time().plusMinutes(window.durationMinutes()))) {
				return true;
			}
		}

		return false;
	}

	public List<AppointmentTime> findWindows(int docID) {
		return AppointmentTimeWrapper.unwrapAll(
				findWindows(AppointmentTimeWrapper.wrapAll(appointments.get(docID).stream().map(Appointment::getDate)).toList())
		).toList();
	}

	public List<AppointmentTime> findWindows(int docID, XMLGregorianCalendar start, XMLGregorianCalendar stop) {
		return findWindows(docID, AppointmentTimeWrapper.wrap(start), AppointmentTimeWrapper.wrap(stop));
	}
	public List<AppointmentTime> findWindows(int docID, LocalDateTime start, LocalDateTime stop) {
		return AppointmentTimeWrapper.unwrapAll(findWindowsWrapped(docID, start, stop)).toList();
	}
	private List<AppointmentTimeWrapper> findWindowsWrapped(int docID, LocalDateTime start, LocalDateTime stop) {
		var appointments = findAppointments(docID, start, stop).stream().map(Appointment::getDate);

		var forSearch = new ArrayList<>(AppointmentTimeWrapper.wrapAll(appointments).toList());

		forSearch.add(0, new AppointmentTimeWrapper(start, 0));
		forSearch.add(new AppointmentTimeWrapper(stop, 0));

		return findWindows(forSearch);
	}

	public List<Appointment> findAppointments(int docID, LocalDateTime start, LocalDateTime stop) {
		var result = new ArrayList<Appointment>();

		if (!appointments.containsKey(docID)) {
			return result;
		}

		for (Appointment appointment : appointments.get(docID)) {
			var t = new AppointmentTimeWrapper(appointment.getDate());

			var aStart = t.time();
			var aStop = aStart.plusMinutes(t.durationMinutes());

			if (aStop.isAfter(start) && aStop.isBefore(stop) || aStart.isBefore(start) && aStop.isAfter(stop)) {
				result.add(appointment);
			}
		}

		return result;
	}

	private List<AppointmentTimeWrapper> findWindows(List<AppointmentTimeWrapper> appointments) {
		var m = new TreeMap<>(appointments.stream().collect(Collectors.groupingBy(a -> a.time().toLocalDate())));

		var results = new ArrayList<AppointmentTimeWrapper>();

		for (Map.Entry<LocalDate, List<AppointmentTimeWrapper>> day : m.entrySet()) {
			results.addAll(findWindowsInDay(day.getKey(), day.getValue()));
		}

		return results;
	}

	private static List<AppointmentTimeWrapper> findWindowsInDay(LocalDate date, List<AppointmentTimeWrapper> appointments) {
		appointments = appointments.stream().sorted(Comparator.comparing(AppointmentTimeWrapper::time)).toList();

		var result = new ArrayList<AppointmentTimeWrapper>();

		BiConsumer<LocalTime, LocalTime> addIf = (previous, current) -> {
			var dur = Duration.between(previous, current);

			if (current.getHour() == 23 && current.getMinute() == 59 && current.getSecond() == 59 && current.getNano() >= 999_000_000) {
				dur = dur.plusNanos(1_000_000_000 - dur.getNano());
			}

			int m = (int) dur.toMinutes();

			if (m > 0) {
				result.add(new AppointmentTimeWrapper(LocalDateTime.of(date, previous), m));
			}
		};

		LocalTime previous = appointments.get(0).time().toLocalTime();

		for (AppointmentTimeWrapper a : appointments) {
			var localTime = a.time().toLocalTime();

			addIf.accept(previous, localTime);

			previous = localTime.plusMinutes(a.durationMinutes());
		}

		return result;
	}
}
