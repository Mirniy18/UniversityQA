package com.cs41.dudnyk.lab34;

import com.lab3.dudnyk.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocController {
	private final DocRepository docRepository;

	@Autowired
	public DocController(DocRepository docRepository) {
		this.docRepository = docRepository;
	}

	@GetMapping("/doc")
	GetDoctorsResponse getDoctors() {
		var responce = new GetDoctorsResponse();
		responce.getDoctors().addAll(docRepository.getDoctors());
		return responce;
	}

	@PutMapping("/appointment")
	PutAppointmentResponse putAppointment(@RequestBody PutAppointmentRequest request) {
		var responce = new PutAppointmentResponse();
		responce.setId(docRepository.addAppointment(request.getDoctorId(), request.getPatient(), request.getTime()).getId());
		return responce;
	}

	@DeleteMapping("/appointment")
	void delAppointment(@RequestBody DelAppointmentRequest request) {
		var dbg = docRepository.removeAppointment(request.getDoctorId(), request.getAppointmentId());
	}

	@GetMapping("/windows")
	GetWindowsResponce getWindows(@RequestBody GetWindowsRequest request) {
		var response = new GetWindowsResponce();
		response.getWindows().addAll(docRepository.findWindows(request.getDoctorId(), request.getStart(), request.getStop()));
		return response;
	}
}
