package com.fantasy.competition.controller;

import com.fantasy.competition.dto.StandingDto;
import com.fantasy.competition.repository.StandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StandingController {

    private final StandingRepository repo;

    @GetMapping
    public List<StandingDto> list(@RequestParam UUID seasonId) {
        return repo.findBySeasonIdOrderByPointsDescGoalsForDesc(seasonId).stream().map(StandingDto::from).toList();
    }
}
