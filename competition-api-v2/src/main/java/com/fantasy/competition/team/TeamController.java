package com.fantasy.competition.team;

import com.fantasy.competition.team.TeamDtos.KitDto;
import com.fantasy.competition.team.TeamDtos.KitRequest;
import com.fantasy.competition.team.TeamDtos.NationDto;
import com.fantasy.competition.team.TeamDtos.NationRequest;
import com.fantasy.competition.team.TeamDtos.ProfileDto;
import com.fantasy.competition.team.TeamDtos.ProfileRequest;
import com.fantasy.competition.team.TeamDtos.StadiumDto;
import com.fantasy.competition.team.TeamDtos.StadiumRequest;
import com.fantasy.competition.team.TeamDtos.TeamDetail;
import com.fantasy.competition.team.TeamDtos.TeamDto;
import com.fantasy.competition.team.TeamDtos.TeamRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    // ---- public reads ----

    @GetMapping("/api/universes/{key}/nations")
    public List<NationDto> nations(@PathVariable String key) { return service.nations(key); }

    @GetMapping("/api/nations/{id}")
    public NationDto nation(@PathVariable Long id) { return service.nation(id); }

    @GetMapping("/api/nations/{id}/teams")
    public List<TeamDto> nationTeams(@PathVariable Long id) { return service.teamsOfNation(id); }

    @GetMapping("/api/universes/{key}/stadiums")
    public List<StadiumDto> stadiums(@PathVariable String key) { return service.stadiums(key); }

    @GetMapping("/api/universes/{key}/teams")
    public List<TeamDto> teams(@PathVariable String key, @RequestParam(required = false) TeamType type) { return service.teams(key, type); }

    @GetMapping("/api/teams/{id}")
    public TeamDetail team(@PathVariable Long id) { return service.teamDetail(id); }

    // ---- admin writes ----

    @PostMapping("/api/admin/universes/{key}/nations")
    @ResponseStatus(HttpStatus.CREATED)
    public NationDto createNation(@PathVariable String key, @Valid @RequestBody NationRequest req) { return service.createNation(key, req); }

    @PutMapping("/api/admin/nations/{id}")
    public NationDto updateNation(@PathVariable Long id, @Valid @RequestBody NationRequest req) { return service.updateNation(id, req); }

    @DeleteMapping("/api/admin/nations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNation(@PathVariable Long id) { service.deleteNation(id); }

    @PostMapping("/api/admin/universes/{key}/stadiums")
    @ResponseStatus(HttpStatus.CREATED)
    public StadiumDto createStadium(@PathVariable String key, @Valid @RequestBody StadiumRequest req) { return service.createStadium(key, req); }

    @PutMapping("/api/admin/stadiums/{id}")
    public StadiumDto updateStadium(@PathVariable Long id, @Valid @RequestBody StadiumRequest req) { return service.updateStadium(id, req); }

    @DeleteMapping("/api/admin/stadiums/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStadium(@PathVariable Long id) { service.deleteStadium(id); }

    @PostMapping("/api/admin/universes/{key}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto createTeam(@PathVariable String key, @Valid @RequestBody TeamRequest req) { return service.createTeam(key, req); }

    @PutMapping("/api/admin/teams/{id}")
    public TeamDto updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest req) { return service.updateTeam(id, req); }

    @DeleteMapping("/api/admin/teams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable Long id) { service.deleteTeam(id); }

    @PutMapping("/api/admin/teams/{id}/profiles")
    public ProfileDto saveProfile(@PathVariable Long id, @RequestBody ProfileRequest req) { return service.saveProfile(id, req); }

    @DeleteMapping("/api/admin/team-profiles/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable Long profileId) { service.deleteProfile(profileId); }

    @PutMapping("/api/admin/teams/{id}/kits")
    public KitDto saveKit(@PathVariable Long id, @RequestBody KitRequest req) { return service.saveKit(id, req); }

    @DeleteMapping("/api/admin/kits/{kitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKit(@PathVariable Long kitId) { service.deleteKit(kitId); }
}
