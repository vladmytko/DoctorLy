package com.example.vladyslav.controller;

import com.example.vladyslav.dto.ReviewDTO;
import com.example.vladyslav.model.User;
import com.example.vladyslav.requests.ReviewCreateRequest;
import com.example.vladyslav.service.ReviewService;
import com.example.vladyslav.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> list(@PathVariable String doctorId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(reviewService.listForDoctor(doctorId, page, size));
    }

    @PostMapping()
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewCreateRequest request){
        return new ResponseEntity<>(reviewService.createReviewForDoctor(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String reviewId,
                                       Authentication auth){
        User user = userService.getCurrentUser(auth);
        reviewService.deleteReview(reviewId,user);
        return ResponseEntity.noContent().build();
    }
}
