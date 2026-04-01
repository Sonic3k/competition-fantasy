package com.fantasy.competition.controller;

import com.fantasy.competition.dto.StageDto;
import com.fantasy.competition.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stages")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StageController {

    private final StageRepository repo;

    @GetMapping
    public List<StageDto> list(@RequestParam UUID seasonId) {
        return repo.findBySeasonIdOrderByOrderNumber(seasonId).stream().map(StageDto::from).toList();
    }
}
