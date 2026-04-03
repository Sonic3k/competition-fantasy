package com.fantasy.competition.controller;

import com.fantasy.competition.dto.CollectionDto;
import com.fantasy.competition.entity.Collection;
import com.fantasy.competition.repository.CollectionRepository;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionRepository repo;
    private final UniverseRepository universeRepo;

    @GetMapping
    public List<CollectionDto> list(@RequestParam(required = false) UUID parentId,
                                    @RequestParam(required = false) UUID universeId,
                                    @RequestParam(required = false) String type) {
        if (type != null) return repo.findByTypeOrderByName(Collection.CollectionType.valueOf(type)).stream().map(CollectionDto::from).toList();
        if (universeId != null) return repo.findByUniverseIdOrderByName(universeId).stream().map(CollectionDto::from).toList();
        if (parentId != null) return repo.findByParentIdOrderByName(parentId).stream().map(CollectionDto::from).toList();
        return repo.findByParentIsNullOrderByName().stream().map(CollectionDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(CollectionDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CollectionDto create(@RequestBody CreateCollectionRequest req) {
        Collection c = new Collection();
        c.setName(req.name());
        c.setDescription(req.description());
        c.setType(req.type() != null ? Collection.CollectionType.valueOf(req.type()) : Collection.CollectionType.GENERAL);
        if (req.parentId() != null) repo.findById(req.parentId()).ifPresent(c::setParent);
        if (req.universeId() != null) universeRepo.findById(req.universeId()).ifPresent(c::setUniverse);
        return CollectionDto.from(repo.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> update(@PathVariable UUID id, @RequestBody CreateCollectionRequest req) {
        return repo.findById(id).map(c -> {
            if (req.name() != null) c.setName(req.name());
            if (req.description() != null) c.setDescription(req.description());
            if (req.type() != null) c.setType(Collection.CollectionType.valueOf(req.type()));
            return ResponseEntity.ok(CollectionDto.from(repo.save(c)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    record CreateCollectionRequest(String name, String description, String type, UUID parentId, UUID universeId) {}
}
