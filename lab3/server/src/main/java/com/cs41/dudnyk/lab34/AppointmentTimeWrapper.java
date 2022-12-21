package com.cs41.dudnyk.lab34;

import com.lab3.dudnyk.AppointmentTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

public record AppointmentTimeWrapper(LocalDateTime time, int durationMinutes) {
	public AppointmentTimeWrapper(AppointmentTime t) {
		this(wrap(t.getTime()), t.getDurationMinutes());
	}

	public AppointmentTime unwrap() {
		var r = new AppointmentTime();
		r.setTime(toXML(this.time));
		r.setDurationMinutes(this.durationMinutes);
		return r;
	}

	public static Stream<AppointmentTimeWrapper> wrapAll(Stream<AppointmentTime> ts) {
		return ts.map(AppointmentTimeWrapper::new);
	}
	public static Stream<AppointmentTimeWrapper> wrapAll(List<AppointmentTime> ts) {
		return wrapAll(ts.stream());
	}

	public static Stream<AppointmentTime> unwrapAll(Stream<AppointmentTimeWrapper> ts) {
		return ts.map(AppointmentTimeWrapper::unwrap);
	}
	public static Stream<AppointmentTime> unwrapAll(List<AppointmentTimeWrapper> ts) {
		return unwrapAll(ts.stream());
	}

	public static LocalDateTime wrap(XMLGregorianCalendar t) {
		var g = t.toGregorianCalendar();

		return Instant.ofEpochMilli(g.getTime().getTime()).atZone(ZoneId.of(g.getTimeZone().getID())).toLocalDateTime();
	}

	private static XMLGregorianCalendar toXML(LocalDateTime t) {
		var g = new GregorianCalendar();

		g.setTime(Date.from(t.atZone(ZoneId.systemDefault()).toInstant()));

		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(g);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
	}
}
