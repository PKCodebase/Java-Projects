package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.CreateOccasionRequest;
import com.mcd.plantation.dto.response.OccasionResponse;
import com.mcd.plantation.dto.response.TreeSpeciesResponse;
import com.mcd.plantation.entity.Occasion;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.OccasionRepository;
import com.mcd.plantation.repository.TreeSpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class OccasionService {

    private final OccasionRepository occasionRepo;
    private final TreeSpeciesRepository speciesRepo;
    private final TreeSpeciesService speciesService;

    @Transactional(readOnly = true)
    public List<OccasionResponse> getAll() {
        return occasionRepo.findByIsActiveTrueOrderByDisplayOrder().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OccasionResponse> getAllAdmin() {
        return occasionRepo.findAllByOrderByDisplayOrder().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TreeSpeciesResponse> getSpeciesForOccasion(UUID occasionId) {
        occasionRepo.findById(occasionId)
            .orElseThrow(() -> ResourceNotFoundException.of("Occasion", occasionId));
        return speciesRepo.findActiveByOccasionId(occasionId).stream()
            .map(speciesService::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OccasionResponse create(CreateOccasionRequest req) {
        Occasion occ = Occasion.builder()
            .name(req.name())
            .description(req.description())
            .emojiCode(req.emojiCode())
            .displayOrder(req.displayOrder().shortValue())
            .isActive(req.isActive() == null || req.isActive())
            .build();
        return toResponse(occasionRepo.save(occ));
    }

    @Transactional
    public OccasionResponse update(UUID occasionId, CreateOccasionRequest req) {
        Occasion occ = occasionRepo.findById(occasionId)
            .orElseThrow(() -> ResourceNotFoundException.of("Occasion", occasionId));
        occ.setName(req.name());
        if (req.description() != null) occ.setDescription(req.description());
        occ.setEmojiCode(req.emojiCode());
        occ.setDisplayOrder(req.displayOrder().shortValue());
        if (req.isActive() != null) occ.setActive(req.isActive());
        return toResponse(occasionRepo.save(occ));
    }

    public OccasionResponse toResponse(Occasion occ) {
        return new OccasionResponse(
            occ.getOccasionId(), occ.getName(), occ.getDescription(),
            occ.getEmojiCode(), occ.getDisplayOrder(), occ.isActive());
    }
}
