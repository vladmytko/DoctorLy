package com.example.vladyslav.controller;

import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.model.enums.AppointmentType;
import com.example.vladyslav.model.enums.LanguageCode;
import com.example.vladyslav.requests.DoctorRegisterRequest;
import com.example.vladyslav.search.DoctorSearchCriteria;
import com.example.vladyslav.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {


    private final DoctorService doctorService;


    @GetMapping("/all")
    public ResponseEntity<Page<DoctorDTO>> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size){
        Page<DoctorDTO> dto = doctorService.getAllDoctors(page, size);
        return ResponseEntity.ok(dto);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
            )
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @ModelAttribute DoctorRegisterRequest request){
        return new ResponseEntity<>(doctorService.createDoctor(request), HttpStatus.CREATED);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable String id){
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

//    @GetMapping
//    public ResponseEntity<Page<DoctorDTO>> getDoctorsByClinicAndRating(@RequestParam(required = false) String clinicId,
//                                                                       @RequestParam(required = false) @Min(0) Integer minRating,
//                                                                       @PageableDefault(size = 20, sort = "lastName") Pageable pageable){
//        return ResponseEntity.ok(doctorService.search(clinicId, minRating, pageable));
//    }

    @GetMapping("/last-name/{lastName}")
    public ResponseEntity<Page<DoctorDTO>> getDoctorByLastName(@PathVariable String lastName,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size){
        Page<DoctorDTO> dto = doctorService.getDoctorByLastName(lastName, page, size);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search-by-speciality")
    public Page<DoctorDTO> findDoctorsBySpeciality(
            @RequestParam(required = false) String specialityId,
            @RequestParam(required = false) String specialityTitle,
            Pageable pageable
            )   {
        if(specialityId != null) return doctorService.findDoctorsBySpecialityId(specialityId, pageable);
        if(specialityTitle != null && !specialityTitle.isBlank()) return doctorService.findBySpecialityTitle(specialityTitle, pageable);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide specialityId or speciality title");
    }

    @GetMapping
    public ResponseEntity<Page<DoctorDTO>> searchDoctor(
            @RequestParam(required = false) String specialityId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String postCode,
            @RequestParam(required = false) String clinicId,
//            @RequestParam(required = false) Double lng,
//            @RequestParam(required = false) Double lat,
//            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) Integer minFee,
            @RequestParam(required = false) Integer maxFee,
            @RequestParam(required = false) String q,
            Pageable pageable
    )   {
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
            specialityId, language, city, postCode, clinicId, minFee, maxFee, q
        );
        return ResponseEntity.ok(doctorService.search(criteria, pageable));
    }

    @PutMapping("/update/{doctorId}")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable String doctorId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String specialityId,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) List<LanguageCode> languages,
            @RequestParam(required = false) Integer consultationFee,
            @RequestParam(required = false) List<AppointmentType> appointmentTypes)
    {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, firstName, lastName, specialityId, phoneNumber, dateOfBirth, bio, languages, consultationFee, appointmentTypes));
    }



}
