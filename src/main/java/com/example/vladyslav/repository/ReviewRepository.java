package com.example.vladyslav.repository;

import com.example.vladyslav.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByDoctorIdOrderByCreatedAtDesc(String doctorId, Pageable pageable);

    boolean existsByDoctorIdAndPatientId(String doctorId, String patientId);

    Optional<Review> findByDoctorIdAndPatientId(String doctorId, String patientId);

    Page<Review> findByDoctorId(String doctorId,  Pageable pageable);

    List<Review> findTop3ByDoctorIdOrderByCreatedAtDesc(String doctorId);
}
