package com.example.vladyslav.service;

import com.example.vladyslav.model.Speciality;
import com.example.vladyslav.repository.SpecialityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialityService {

    @Autowired
    private SpecialityRepository specialityRepository;

    public List<Speciality> getAll(){
        return specialityRepository.findAll();
    }

    public Speciality createSpeciality(Speciality speciality){


        // Validate input
        if(speciality.getTitle() == null || speciality.getTitle().trim().isBlank()){
           throw new IllegalArgumentException("Title must be provided");
        }

        // Normalise title (trim)
        String normaliseTitle = speciality.getTitle().trim();

        // Prevent duplicates. Use repository lookup;
        try{
            specialityRepository.findByTitle(normaliseTitle).ifPresent(
                    existing -> {
                        throw new IllegalStateException("Speciality with title '" + normaliseTitle + "' already exists");
                    }
            );
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Repository found more than one - duplicates already exists
            throw new IllegalStateException("Speciality with title '" + normaliseTitle + "' already exists");
        }

        Speciality newSpeciality = Speciality.builder()
                .title(normaliseTitle).build();



        return specialityRepository.save(newSpeciality);
    }

    public Speciality getSpecialityById(String specialityId){
        return specialityRepository.findById(specialityId).orElseThrow(()-> new RuntimeException("Speciality not found!"));
    }

    public Speciality getSpecialityByTitle(String title){
        return specialityRepository.findByTitle(title).orElseThrow(()-> new RuntimeException("Speciality not found by title: " + title));
    }

    /**
     * Type-ahead friendly search by prefix
     *
     * @param q     User-entered query text. It trims it and enforce a 3-char minimum
     *              to avoid noisy results and excessive DB calls during typing
     *
     * @param limit Max number of results returned. It caps it at 25 for safety.
     *
     * @return      A sorted (asc by title) list of at most 'limit' specialities
     *              whose titles starts with the given query (case-sensitive).
     *
     */
    public List<Speciality> searchByPrefix(String q, int limit) {
        // Normalise user input: handle null and trim whitespace
        String query =  q == null ? "" : q.trim();

        // Debounce-friendly guard: only search after 3+ characters
        if (query.length() < 3) return List.of();

        // Defensive limit: avoid returning/processing too many items
        int safeLimit = Math.max(1, Math.min(limit, 25));

        // Page first N results, alphabetically by title for stable UX
        Pageable page = PageRequest.of(0, safeLimit, Sort.by("title").ascending());

        // Delegate to Spring Data: users 'findByTitleStartsWithIgnoreCase' which maps
        // to a case-insensitive prefix query on the 'title' field.
        return specialityRepository.findByTitleStartsWithIgnoreCase(query, page);
    }





}
