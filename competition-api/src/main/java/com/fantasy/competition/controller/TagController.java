package com.fantasy.competition.controller;

import com.fantasy.competition.dto.TagDto;
import com.fantasy.competition.entity.Tag;
import com.fantasy.competition.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin("*")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository repo;

    @GetMapping
    public List<TagDto> list() {
        return repo.findAllByOrderByName().stream().map(TagDto::from).toList();
    }

    @PostMapping
    public TagDto create(@RequestBody Tag tag) {
        return TagDto.from(repo.save(tag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> update(@PathVariable UUID id, @RequestBody Tag body) {
        return repo.findById(id).map(t -> {
            t.setName(body.getName());
            t.setColor(body.getColor());
            return ResponseEntity.ok(TagDto.from(repo.save(t)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
