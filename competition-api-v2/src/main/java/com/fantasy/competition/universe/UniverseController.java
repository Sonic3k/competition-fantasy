package com.fantasy.competition.universe;

import com.fantasy.competition.universe.UniverseDtos.UniverseDto;
import com.fantasy.competition.universe.UniverseDtos.UniverseRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UniverseController {

    private final UniverseService service;

    public UniverseController(UniverseService service) {
        this.service = service;
    }

    @GetMapping("/api/universes")
    public List<UniverseDto> list() { return service.list(); }

    @GetMapping("/api/universes/{key}")
    public UniverseDto get(@PathVariable String key) { return service.get(key); }

    @PostMapping("/api/admin/universes")
    @ResponseStatus(HttpStatus.CREATED)
    public UniverseDto create(@Valid @RequestBody UniverseRequest req) { return service.create(req); }

    @PutMapping("/api/admin/universes/{id}")
    public UniverseDto update(@PathVariable Long id, @Valid @RequestBody UniverseRequest req) { return service.update(id, req); }

    @DeleteMapping("/api/admin/universes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
