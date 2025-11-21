package com.example.vladyslav.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorSearchCriteria {

    private String specialityId;
    private String language;
    private String city;
    private String postCode;
    private String clinicId;

//    private Double lat;
//    private Double lng;
//    private Double radiusKm;

    private Integer minFee;
    private Integer maxFee;

    private String q; // text search: name, bio
}
