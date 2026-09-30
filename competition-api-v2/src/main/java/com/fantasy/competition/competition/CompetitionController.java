package com.fantasy.competition.competition;

import com.fantasy.competition.competition.CompetitionDtos.CompetitionDetail;
import com.fantasy.competition.competition.CompetitionDtos.CompetitionDto;
import com.fantasy.competition.competition.CompetitionDtos.SeasonSummary;
import com.fantasy.competition.standings.RankedRow;
import com.fantasy.competition.standings.StandingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Public, read-only competition endpoints used by the viewer. */
@RestController
public class CompetitionController {

    public record TableAsOf(long stageGroupId, Integer upToRound, List<RankedRow> rows, Map<Long, SeasonView.TeamRef> teams) {}
    public record MatchWithTeams(SeasonView.MatchView match, Map<Long, SeasonView.TeamRef> teams) {}
    public record HeadToHead(long teamA, long teamB, int winsA, int winsB, int draws, List<SeasonView.MatchView> matches, Map<Long, SeasonView.TeamRef> teams) {}

    private final CompetitionService service;
    private final SeasonViewService views;
    private final StandingsService standings;
    private final MatchRepository matches;
    private final SeasonRepository seasons;

    public CompetitionController(CompetitionService service, SeasonViewService views, StandingsService standings,
                                 MatchRepository matches, SeasonRepository seasons) {
        this.service = service;
        this.views = views;
        this.standings = standings;
        this.matches = matches;
        this.seasons = seasons;
    }

    @GetMapping("/api/universes/{key}/competitions")
    public List<CompetitionDto> list(@PathVariable String key) { return service.list(key); }

    @GetMapping("/api/competitions/{id}")
    public CompetitionDetail detail(@PathVariable Long id) { return service.detail(id); }

    @GetMapping("/api/competitions/{id}/seasons")
    public List<SeasonSummary> seasons(@PathVariable Long id) {
        service.competition(id);
        return service.summaries(seasons.findByCompetitionIdOrderByYearDescNameDesc(id));
    }

    @GetMapping("/api/seasons/{id}")
    public SeasonView season(@PathVariable Long id) { return views.build(id); }

    @GetMapping("/api/seasons/{id}/summary")
    public SeasonSummary seasonSummary(@PathVariable Long id) { return service.summary(id); }

    @GetMapping("/api/stage-groups/{id}/table")
    public TableAsOf table(@PathVariable Long id, @RequestParam(required = false) Integer upToRound) {
        List<RankedRow> rows = standings.computeAsOf(id, upToRound);
        Set<Long> teamIds = new LinkedHashSet<>();
        rows.forEach(r -> teamIds.add(r.teamId()));
        return new TableAsOf(id, upToRound, rows, views.teamRefs(teamIds, null));
    }

    @GetMapping("/api/matches/{id}")
    public MatchWithTeams match(@PathVariable Long id) {
        Match m = service.match(id);
        Season s = seasons.findById(m.getSeasonId()).orElse(null);
        Set<Long> ids = new LinkedHashSet<>();
        if (m.getHomeTeamId() != null) ids.add(m.getHomeTeamId());
        if (m.getAwayTeamId() != null) ids.add(m.getAwayTeamId());
        return new MatchWithTeams(SeasonView.MatchView.of(m), views.teamRefs(ids, s == null ? null : s.getYear()));
    }

    @GetMapping("/api/teams/{a}/head-to-head/{b}")
    public HeadToHead headToHead(@PathVariable Long a, @PathVariable Long b) {
        List<Match> list = matches.findHeadToHead(a, b);
        int winsA = 0, winsB = 0, draws = 0;
        for (Match m : list) {
            if (!m.hasScore()) continue;
            Long w = m.getWinnerTeamId();
            if (w == null) draws++;
            else if (w.equals(a)) winsA++;
            else if (w.equals(b)) winsB++;
        }
        return new HeadToHead(a, b, winsA, winsB, draws, list.stream().map(SeasonView.MatchView::of).toList(),
                views.teamRefs(List.of(a, b), null));
    }
}
