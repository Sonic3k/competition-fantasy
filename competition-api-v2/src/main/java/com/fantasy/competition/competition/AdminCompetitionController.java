package com.fantasy.competition.competition;

import com.fantasy.competition.competition.CompetitionDtos.CompetitionDto;
import com.fantasy.competition.competition.CompetitionDtos.CompetitionRequest;
import com.fantasy.competition.competition.CompetitionDtos.GroupRequest;
import com.fantasy.competition.competition.CompetitionDtos.HonourRequest;
import com.fantasy.competition.competition.CompetitionDtos.KoRoundRequest;
import com.fantasy.competition.competition.CompetitionDtos.MatchRequest;
import com.fantasy.competition.competition.CompetitionDtos.ResultRequest;
import com.fantasy.competition.competition.CompetitionDtos.RoundRequest;
import com.fantasy.competition.competition.CompetitionDtos.SeasonRequest;
import com.fantasy.competition.competition.CompetitionDtos.SeasonSummary;
import com.fantasy.competition.competition.CompetitionDtos.SeasonTeamInput;
import com.fantasy.competition.competition.CompetitionDtos.StageRequest;
import com.fantasy.competition.competition.CompetitionDtos.TieRequest;
import com.fantasy.competition.standings.StandingsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Admin writes for competitions, seasons and their structure. Every endpoint needs the admin JWT. */
@RestController
@RequestMapping("/api/admin")
public class AdminCompetitionController {

    public record IdDto(Long id) {}
    public record CountDto(int count) {}
    public record RecordedStandingRequest(Integer checkpointRound, List<StandingsService.RecordedRowInput> rows) {}

    private final CompetitionService service;
    private final StandingsService standings;
    private final SeasonViewService views;

    public AdminCompetitionController(CompetitionService service, StandingsService standings, SeasonViewService views) {
        this.service = service;
        this.standings = standings;
        this.views = views;
    }

    // competitions
    @PostMapping("/universes/{key}/competitions")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionDto createCompetition(@PathVariable String key, @Valid @RequestBody CompetitionRequest req) { return service.create(key, req); }

    @PutMapping("/competitions/{id}")
    public CompetitionDto updateCompetition(@PathVariable Long id, @Valid @RequestBody CompetitionRequest req) { return service.update(id, req); }

    @DeleteMapping("/competitions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompetition(@PathVariable Long id) { service.delete(id); }

    // seasons
    @PostMapping("/competitions/{id}/seasons")
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonSummary createSeason(@PathVariable Long id, @Valid @RequestBody SeasonRequest req) { return service.createSeason(id, req); }

    @PutMapping("/seasons/{id}")
    public SeasonSummary updateSeason(@PathVariable Long id, @Valid @RequestBody SeasonRequest req) { return service.updateSeason(id, req); }

    @DeleteMapping("/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeason(@PathVariable Long id) { service.deleteSeason(id); }

    @PutMapping("/seasons/{id}/teams")
    public CountDto setSeasonTeams(@PathVariable Long id, @RequestBody List<SeasonTeamInput> teams) { return new CountDto(service.setSeasonTeams(id, teams)); }

    @PostMapping("/seasons/{id}/scaffold")
    public SeasonView scaffold(@PathVariable Long id) {
        service.scaffold(service.season(id));
        return views.build(id);
    }

    @PostMapping("/seasons/{id}/recalculate")
    public CountDto recalculate(@PathVariable Long id) { return new CountDto(standings.recalculateSeason(id)); }

    // structure
    @PostMapping("/seasons/{id}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addStage(@PathVariable Long id, @Valid @RequestBody StageRequest req) { return new IdDto(service.addStage(id, req).getId()); }

    @DeleteMapping("/stages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStage(@PathVariable Long id) { service.deleteStage(id); }

    @PostMapping("/stages/{id}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addGroup(@PathVariable Long id, @Valid @RequestBody GroupRequest req) { return new IdDto(service.addGroup(id, req).getId()); }

    @PutMapping("/stage-groups/{id}/teams")
    public Map<String, Object> setGroupTeams(@PathVariable Long id, @RequestBody List<Long> teamIds) {
        service.setGroupTeams(id, teamIds);
        return Map.of("stageGroupId", id, "teams", teamIds.size());
    }

    @DeleteMapping("/stage-groups/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id) { service.deleteGroup(id); }

    @PostMapping("/stage-groups/{id}/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addRound(@PathVariable Long id, @RequestBody RoundRequest req) { return new IdDto(service.addRound(id, req).getId()); }

    @DeleteMapping("/rounds/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRound(@PathVariable Long id) { service.deleteRound(id); }

    @PostMapping("/stages/{id}/ko-rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addKoRound(@PathVariable Long id, @Valid @RequestBody KoRoundRequest req) { return new IdDto(service.addKoRound(id, req).getId()); }

    @DeleteMapping("/ko-rounds/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKoRound(@PathVariable Long id) { service.deleteKoRound(id); }

    @PostMapping("/ko-rounds/{id}/ties")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addTie(@PathVariable Long id, @RequestBody TieRequest req) { return new IdDto(service.addTie(id, req).getId()); }

    @PutMapping("/ties/{id}")
    public IdDto updateTie(@PathVariable Long id, @RequestBody TieRequest req) { return new IdDto(service.updateTie(id, req).getId()); }

    @DeleteMapping("/ties/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTie(@PathVariable Long id) { service.deleteTie(id); }

    @PostMapping("/seasons/{id}/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonView.MatchView addMatch(@PathVariable Long id, @RequestBody MatchRequest req) { return SeasonView.MatchView.of(service.addMatch(id, req)); }

    @PutMapping("/matches/{id}")
    public SeasonView.MatchView updateMatch(@PathVariable Long id, @RequestBody MatchRequest req) { return SeasonView.MatchView.of(service.updateMatch(id, req)); }

    @PutMapping("/matches/{id}/result")
    public SeasonView.MatchView setResult(@PathVariable Long id, @RequestBody ResultRequest req) { return SeasonView.MatchView.of(service.setResult(id, req)); }

    @DeleteMapping("/matches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) { service.deleteMatch(id); }

    // standings
    @PostMapping("/stage-groups/{id}/recalculate")
    public IdDto recalculateGroup(@PathVariable Long id) { return new IdDto(standings.recalculate(id).getId()); }

    @PutMapping("/stage-groups/{id}/recorded")
    public IdDto saveRecorded(@PathVariable Long id, @RequestBody RecordedStandingRequest req) {
        return new IdDto(standings.saveRecorded(id, req.checkpointRound() == null ? 0 : req.checkpointRound(), req.rows()).getId());
    }

    @DeleteMapping("/stage-groups/{id}/recorded/{checkpointRound}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecorded(@PathVariable Long id, @PathVariable Integer checkpointRound) { standings.deleteRecorded(id, checkpointRound); }

    // honours
    @PostMapping("/seasons/{id}/honours")
    @ResponseStatus(HttpStatus.CREATED)
    public IdDto addHonour(@PathVariable Long id, @RequestBody HonourRequest req) { return new IdDto(service.addHonour(id, req).getId()); }

    @DeleteMapping("/honours/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHonour(@PathVariable Long id) { service.deleteHonour(id); }
}
