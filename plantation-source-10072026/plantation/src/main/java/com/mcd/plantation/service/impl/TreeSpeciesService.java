package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.CreateTreeSpeciesRequest;
import com.mcd.plantation.dto.response.OccasionResponse;
import com.mcd.plantation.dto.response.TreeSpeciesResponse;
import com.mcd.plantation.entity.Occasion;
import com.mcd.plantation.entity.TreeSpecies;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.OccasionRepository;
import com.mcd.plantation.repository.TreeSpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class TreeSpeciesService {

    private final TreeSpeciesRepository speciesRepo;
    private final OccasionRepository occasionRepo;

    @Transactional(readOnly = true)
    public List<TreeSpeciesResponse> getAll() {
        return speciesRepo.findAllByOrderByCommonName().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TreeSpeciesResponse> getAllActive() {
        return speciesRepo.findByIsActiveTrueOrderByCommonName().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TreeSpeciesResponse getOne(UUID speciesId) {
        return toResponse(speciesRepo.findById(speciesId)
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", speciesId)));
    }

    @Transactional
    public TreeSpeciesResponse create(CreateTreeSpeciesRequest req) {
        List<Occasion> occasions = resolveOccasions(req.preferredOccasionIds());
        TreeSpecies sp = TreeSpecies.builder()
            .commonName(req.commonName())
            .scientificName(req.scientificName())
            .emojiCode(req.emojiCode())
            .price(req.price())
            .category(req.category())
            .benefits(req.benefits())
            .careNotes(req.careNotes())
            .isActive(true)
            .preferredOccasions(occasions)
            .build();
        return toResponse(speciesRepo.save(sp));
    }

    @Transactional
    public TreeSpeciesResponse update(UUID speciesId, CreateTreeSpeciesRequest req) {
        TreeSpecies sp = speciesRepo.findById(speciesId)
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", speciesId));
        sp.setCommonName(req.commonName());
        if (req.scientificName() != null) sp.setScientificName(req.scientificName());
        if (req.emojiCode()       != null) sp.setEmojiCode(req.emojiCode());
        sp.setPrice(req.price());
        sp.setCategory(req.category());
        if (req.benefits()  != null) sp.setBenefits(req.benefits());
        if (req.careNotes() != null) sp.setCareNotes(req.careNotes());
        if (req.isActive()  != null) sp.setActive(req.isActive());
        if (req.preferredOccasionIds() != null)
            sp.setPreferredOccasions(resolveOccasions(req.preferredOccasionIds()));
        return toResponse(speciesRepo.save(sp));
    }

    @Transactional
    public TreeSpeciesResponse updateOccasions(UUID speciesId, List<UUID> occasionIds) {
        TreeSpecies sp = speciesRepo.findById(speciesId)
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", speciesId));
        sp.setPreferredOccasions(resolveOccasions(occasionIds));
        return toResponse(speciesRepo.save(sp));
    }

    @Transactional
    public void deactivate(UUID speciesId) {
        TreeSpecies sp = speciesRepo.findById(speciesId)
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", speciesId));
        sp.setActive(false);
        speciesRepo.save(sp);
    }

    public TreeSpeciesResponse toResponse(TreeSpecies sp) {
        List<OccasionResponse> occasions = sp.getPreferredOccasions() == null
            ? Collections.emptyList()
            : sp.getPreferredOccasions().stream()
                .map(o -> new OccasionResponse(o.getOccasionId(), o.getName(), o.getDescription(),
                    o.getEmojiCode(), o.getDisplayOrder(), o.isActive()))
                .collect(Collectors.toList());
        return new TreeSpeciesResponse(
            sp.getSpeciesId(), sp.getCommonName(), sp.getScientificName(),
            sp.getEmojiCode(), sp.getPrice(), sp.getCategory(),
            sp.getBenefits(), sp.getCareNotes(), sp.isActive(), occasions);
    }

    private List<Occasion> resolveOccasions(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return occasionRepo.findAllById(ids);
    }
}
