package com.cs41.dudnyk.lab34;

import com.lab3.dudnyk.Doctor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTests {
	@Test
	void testEmptyScheduleFullDay() {
		var r = new DocRepository();
		r.initData();

		var today = LocalDate.now();

		var t1 = LocalDateTime.of(today, LocalTime.MIN);
		var t2 = LocalDateTime.of(today, LocalTime.MAX);

		for (Doctor doc : r.getDoctors()) {
			var a = r.findWindows(doc.getId(), t1, t2).stream().map(AppointmentTimeWrapper::new).toList();
			assertEquals(1, a.size());
			var x = a.get(0);
			assertEquals(t1, x.time());
			assertEquals(24 * 60, x.durationMinutes());
		}
	}

	@Test
	void testEmptySchedule() {
		var r = new DocRepository();
		r.initData();

		var t1 = LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0));
		var t2 = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

		for (Doctor doc : r.getDoctors()) {
			var a = r.findWindows(doc.getId(), t1, t2).stream().map(AppointmentTimeWrapper::new).toList();
			assertEquals(1, a.size());
			var x = a.get(0);
			assertEquals(t1, x.time());
			assertEquals(t2, x.time().plusMinutes(x.durationMinutes()));
			assertEquals(Duration.between(t1, t2).toMinutes(), x.durationMinutes());
		}
	}

	@Test
	void testAppointmentDel() {
		var r = new DocRepository();
		r.initData();

		var today = LocalDate.now();

		var t1 = LocalDateTime.of(today, LocalTime.of(1, 0));
		var t2 = LocalDateTime.of(today, LocalTime.of(13, 0));
		var t3 = LocalDateTime.of(today, LocalTime.of(22, 0));

		for (Doctor doc : r.getDoctors()) {
			var id = r.addAppointment(doc.getId(), "", new AppointmentTimeWrapper(t2, 42).unwrap()).getId();
			assertEquals(1, r.findAppointments(doc.getId(), t1, t3).size());
			assertTrue(r.removeAppointment(doc.getId(), id));
			assertEquals(0, r.findAppointments(doc.getId(), t1, t3).size());
		}
	}

	@Test
	void testAppointmentDelNonexistant() {
		var r = new DocRepository();
		r.initData();

		for (Doctor doc : r.getDoctors()) {
			assertFalse(r.removeAppointment(doc.getId(), 42));
		}
	}

	@Test
	void testWindowSplit() {
		var r = new DocRepository();
		r.initData();

		var today = LocalDate.now();

		var t1 = LocalDateTime.of(today, LocalTime.of(1, 0));
		var t2 = LocalDateTime.of(today, LocalTime.of(13, 0));
		var t3 = LocalDateTime.of(today, LocalTime.of(23, 0));

		int dur = 42;

		for (Doctor doc : r.getDoctors()) {
			r.addAppointment(doc.getId(), "", new AppointmentTimeWrapper(t2, dur).unwrap());

			var a = r.findWindows(doc.getId(), t1, t3).stream().map(AppointmentTimeWrapper::new).toList();
			assertEquals(2, a.size());
			var x = a.get(0);
			var y = a.get(1);

			assertEquals(t1, x.time());
			assertEquals(t2, x.time().plusMinutes(x.durationMinutes()));

			assertEquals(t2.plusMinutes(dur), y.time());
			assertEquals(t3, y.time().plusMinutes(y.durationMinutes()));

			assertEquals(Duration.between(t1, t2).toMinutes(), x.durationMinutes());
			assertEquals(Duration.between(t2, t3).toMinutes() - dur, y.durationMinutes());
		}
	}
}
