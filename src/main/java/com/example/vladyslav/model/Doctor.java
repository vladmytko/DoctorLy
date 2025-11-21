package com.example.vladyslav.model;

import com.example.vladyslav.model.enums.AppointmentType;
import com.example.vladyslav.model.enums.LanguageCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "doctors")
public class Doctor {
    @Id
    private String id;

    @TextIndexed(weight = 2f)
    @NotBlank
    private String firstName;

    @TextIndexed(weight = 2f)
    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @Indexed
    @DBRef
    private Speciality speciality;

    @Pattern(regexp = "^[0-9]{11}$", message = "Invalid phone number format, example '07529201824'")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @TextIndexed
    private String bio;

    @NotBlank(message = "Photo url is required")
    private String imageUrl;

    @Indexed
    @Builder.Default
    private List<LanguageCode> languages = new ArrayList<>();

    private Float averageRating;

    @Indexed
    @Min(0)
    @Builder.Default
    private int consultationFee = 0;

    @Indexed
    @Builder.Default
    private List<AppointmentType> appointmentTypes = new ArrayList<>();

    @DBRef
    private User user;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Indexed
    @DBRef
    private Clinic clinic; // optional - can be null if independent;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location; // longitude, latitude


    public String getFullName() {
        return firstName + " " + lastName;
    }

    @JsonIgnore
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
