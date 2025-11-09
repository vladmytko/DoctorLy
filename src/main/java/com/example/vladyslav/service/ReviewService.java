package com.example.vladyslav.service;

import com.example.vladyslav.dto.ReviewDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.*;
import com.example.vladyslav.model.enums.AppointmentStatus;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.AppointmentRepository;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.repository.PatientRepository;
import com.example.vladyslav.repository.ReviewRepository;
import com.example.vladyslav.requests.ReviewCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public ReviewDTO createReviewForDoctor( ReviewCreateRequest request){
        // 1) Basic validation
        if(request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be 1..5");
        }

        // 2) Ensure doctor exist
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(()-> new NotFoundException("Doctor not found: " + request.getDoctorId()));

        // 3) Ensure patient exist
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(()-> new NotFoundException("Patient not found: " + request.getPatientId()));

        // 4) Retrieve appointment
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(()-> new NotFoundException("Appointment not found: " + request.getAppointmentId()));

        // 4) Ensure that this appointment is ATTENDED

        if(!appointment.getStatus().equals(AppointmentStatus.ATTENDED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only review a doctor after completing an appointment.");
        }

        // 5) Optional: prevent duplicate review per doctor-patient (allow update instead)
        reviewRepository.findByDoctorIdAndPatientId(request.getDoctorId(), request.getPatientId())
                .ifPresent(review -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "You have already reviewed this doctor.");
                });

        // 6) Make sure patient is authorised for this review
        if(!Objects.equals(patient.getId(), appointment.getPatientId())){
            throw new OurException("Patient not authorized for this review action");
        }

        // 7) Validate doctor for this appointment
        if(!Objects.equals(doctor.getId(), appointment.getDoctorId())){
            throw new OurException("Doctor not authorized for review action");
        }


        // 8) Save review
        Review review = Review.builder()
                .doctor(doctor)
                .patient(patient)
                .clinic(doctor.getClinic())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        //9) Save review to doctor
        doctor.getReviews().add(review);
        doctorRepository.save(doctor);

        // 10) Recalculate doctor's average rating
        recalculateDoctorAverage(doctor);

        return toReviewDTO(saved);
    }

    public Page<ReviewDTO> listForDoctor(String doctorId, int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        return reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, pageable)
                .map(this::toReviewDTO);
    }

    public ReviewDTO getReviewById(String reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new NotFoundException("Review not found: " + reviewId));
        return toReviewDTO(review);
    }

    public void deleteReview(String reviewId, User user){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new NotFoundException("Review not found: " + reviewId));

        if(user.getRole().equals(Role.PATIENT)){
            Patient patient = patientRepository.findById(user.getId())
                    .orElseThrow(()-> new NotFoundException("Patient not found with id: " + user.getId()));
            if(!Objects.equals(patient.getId(), review.getPatient().getId())){
                throw new OurException("Patient not authorized for this review action");
            }
        }


        reviewRepository.deleteById(reviewId);
        // When a review is deleted, recalculate doctor average
        doctorRepository.findById(review.getDoctor().getId()).ifPresent(this::recalculateDoctorAverage);
    }

    private void recalculateDoctorAverage(Doctor doctor) {
        var page = reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctor.getId(), Pageable.unpaged());
        double avg = page.getContent().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        doctor.setAverageRating((float) avg);
        doctorRepository.save(doctor);
    }



    public ReviewDTO toReviewDTO(Review review){
        return ReviewDTO.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .patientId(review.getPatient().getId())
                .doctorId(review.getDoctor().getId())
                .clinicId(review.getClinic() != null ? review.getClinic().getId() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
