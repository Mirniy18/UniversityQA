package com.cs41.dudnyk.lab34;

import com.lab3.dudnyk.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DocEndpoint {
	private static final String NAMESPACE_URI = "http://lab3.com/dudnyk";

	private final DocRepository docRepository;

	@Autowired
	public DocEndpoint(DocRepository docRepository) {
		this.docRepository = docRepository;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDoctorsRequest")
	@ResponsePayload
	public GetDoctorsResponse getDoctors() {
		var responce = new GetDoctorsResponse();
		responce.getDoctors().addAll(docRepository.getDoctors());
		return responce;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWindowsRequest")
	@ResponsePayload
	public GetWindowsResponce getWindows(@RequestPayload GetWindowsRequest request) {
		var response = new GetWindowsResponce();
		response.getWindows().addAll(docRepository.findWindows(request.getDoctorId(), request.getStart(), request.getStop()));
		return response;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "putAppointmentRequest")
	@ResponsePayload
	public PutAppointmentResponse putAppointment(@RequestPayload PutAppointmentRequest request) {
		var response = new PutAppointmentResponse();
		response.setId(docRepository.addAppointment(request.getDoctorId(), request.getPatient(), request.getTime()).getId());
		return response;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "delAppointmentRequest")
	@ResponsePayload
	public void delAppointment(@RequestPayload DelAppointmentRequest request) {
		docRepository.removeAppointment(request.getDoctorId(), request.getAppointmentId());
	}
}
