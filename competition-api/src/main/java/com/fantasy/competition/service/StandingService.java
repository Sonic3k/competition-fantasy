package com.fantasy.competition.service;

import com.fantasy.competition.entity.*;
import com.fantasy.competition.format.FormatPresetRegistry;
import com.fantasy.competition.format.TournamentFormat;
import com.fantasy.competition.repository.MatchRepository;
import com.fantasy.competition.repository.SeasonRepository;
import com.fantasy.competition.repository.StandingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Computes CALCULATED standings, grouped per StageGroup, applying the
 * season's configured tie-break order and persisting a final rank per group.
 *
 * For a single-group league (Othelose) this reduces to one table — identical
 * to the previous behaviour. For multi-group tournaments (World Cup, Asian
 * Cup, ...) each group gets its own correctly-ranked table.
 *
 * Tie-break keys supported: GOAL_DIFF, GOALS_FOR, GOALS_AGAINST, WINS,
 * HEAD_TO_HEAD. FAIR_PLAY / DRAW_LOTS are accepted but no-op (we don't track
 * disciplinary data); POINTS is always the implicit primary key.
 */
@Service
@RequiredArgsConstructor
public class StandingService {

    private final SeasonRepository seasonRepo;
    private final MatchRepository matchRepo;
    private final StandingRepository standingRepo;
    private final FormatPresetRegistry presets;
    private final ObjectMapper objectMapper;

    private static final List<String> DEFAULT_TIEBREAK = List.of("GOAL_DIFF", "GOALS_FOR");
    private static final int[] ZERO3 = {0, 0, 0};

    @Transactional
    public List<Standing> recalculate(UUID seasonId, Integer afterRound) {
        Season season = seasonRepo.findById(seasonId).orElseThrow();

        TournamentFormat fmt = resolveFormat(season);
        int pWin = fmt != null ? fmt.pointsWin : 3;
        int pDraw = fmt != null ? fmt.pointsDraw : 1;
        List<String> tieBreak = DEFAULT_TIEBREAK;
        if (fmt != null) {
            TournamentFormat.Phase gp = fmt.firstGroupPhase();
            if (gp != null && gp.tieBreak != null && !gp.tieBreak.isEmpty()) tieBreak = gp.tieBreak;
        }

        // Completed group-stage matches with concrete teams.
        List<Match> matches = matchRepo.findByRoundSeasonId(seasonId).stream()
            .filter(m -> m.getStatus() == Match.MatchStatus.COMPLETED)
            .filter(m -> m.getHomeScore() != null && m.getAwayScore() != null)
            .filter(m -> m.getHomeTeam() != null && m.getAwayTeam() != null)
            .filter(m -> m.getRound() != null && m.getRound().getStageGroup() != null)
            .filter(m -> afterRound == null
                || (m.getRound().getRoundNumber() != null && m.getRound().getRoundNumber() <= afterRound))
            .toList();

        // Replace previous CALCULATED rows for this scope.
        if (afterRound != null) {
            standingRepo.deleteBySeasonIdAndTypeAndAfterRound(seasonId, Standing.StandingType.CALCULATED, afterRound);
        } else {
            standingRepo.deleteBySeasonIdAndTypeAndAfterRoundIsNull(seasonId, Standing.StandingType.CALCULATED);
        }

        // Partition matches by their group (preserve encounter order).
        Map<UUID, StageGroup> groups = new LinkedHashMap<>();
        Map<UUID, List<Match>> byGroup = new LinkedHashMap<>();
        for (Match m : matches) {
            StageGroup g = m.getRound().getStageGroup();
            groups.putIfAbsent(g.getId(), g);
            byGroup.computeIfAbsent(g.getId(), k -> new ArrayList<>()).add(m);
        }

        List<Standing> result = new ArrayList<>();
        for (Map.Entry<UUID, StageGroup> entry : groups.entrySet()) {
            StageGroup group = entry.getValue();
            List<Match> groupMatches = byGroup.get(entry.getKey());

            // Seed every team in the group (so 0-played teams still appear).
            Map<UUID, Standing> table = new LinkedHashMap<>();
            Map<UUID, Team> teams = new LinkedHashMap<>();
            if (group.getTeams() != null) for (Team t : group.getTeams()) teams.putIfAbsent(t.getId(), t);
            for (Match m : groupMatches) {
                teams.putIfAbsent(m.getHomeTeam().getId(), m.getHomeTeam());
                teams.putIfAbsent(m.getAwayTeam().getId(), m.getAwayTeam());
            }
            for (Team t : teams.values()) {
                Standing s = new Standing();
                s.setSeason(season);
                s.setTeam(t);
                s.setType(Standing.StandingType.CALCULATED);
                s.setAfterRound(afterRound);
                s.setStageGroup(group);
                s.setGroupName(group.getName());
                table.put(t.getId(), s);
            }

            for (Match m : groupMatches) {
                Standing home = table.get(m.getHomeTeam().getId());
                Standing away = table.get(m.getAwayTeam().getId());
                if (home == null || away == null) continue;
                int hs = m.getHomeScore(), as = m.getAwayScore();
                home.setPlayed(home.getPlayed() + 1);
                away.setPlayed(away.getPlayed() + 1);
                home.setGoalsFor(home.getGoalsFor() + hs);
                home.setGoalsAgainst(home.getGoalsAgainst() + as);
                away.setGoalsFor(away.getGoalsFor() + as);
                away.setGoalsAgainst(away.getGoalsAgainst() + hs);
                if (hs > as) {
                    home.setWon(home.getWon() + 1); home.setPoints(home.getPoints() + pWin);
                    away.setLost(away.getLost() + 1);
                } else if (hs < as) {
                    away.setWon(away.getWon() + 1); away.setPoints(away.getPoints() + pWin);
                    home.setLost(home.getLost() + 1);
                } else {
                    home.setDrawn(home.getDrawn() + 1); home.setPoints(home.getPoints() + pDraw);
                    away.setDrawn(away.getDrawn() + 1); away.setPoints(away.getPoints() + pDraw);
                }
            }

            List<Standing> ordered = new ArrayList<>(table.values());
            sortGroup(ordered, tieBreak, groupMatches, pWin, pDraw);
            for (int i = 0; i < ordered.size(); i++) ordered.get(i).setRank(i + 1);
            result.addAll(standingRepo.saveAll(ordered));
        }
        return result;
    }

    private TournamentFormat resolveFormat(Season season) {
        if (season.getFormatConfig() != null && !season.getFormatConfig().isBlank()) {
            try {
                return objectMapper.readValue(season.getFormatConfig(), TournamentFormat.class);
            } catch (Exception ignored) { /* fall back to preset */ }
        }
        return presets.get(season.getFormatPresetKey());
    }

    /** Order by points desc, then resolve equal-point runs using the tie-break list. */
    private void sortGroup(List<Standing> list, List<String> tieBreak, List<Match> matches, int pWin, int pDraw) {
        list.sort(Comparator.comparingInt(Standing::getPoints).reversed());
        int i = 0;
        while (i < list.size()) {
            int j = i + 1;
            while (j < list.size() && list.get(j).getPoints() == list.get(i).getPoints()) j++;
            if (j - i > 1) {
                List<Standing> run = new ArrayList<>(list.subList(i, j));
                run.sort(tieComparator(tieBreak, run, matches, pWin, pDraw));
                for (int k = i; k < j; k++) list.set(k, run.get(k - i));
            }
            i = j;
        }
    }

    private Comparator<Standing> tieComparator(List<String> tieBreak, List<Standing> run,
                                               List<Match> matches, int pWin, int pDraw) {
        Comparator<Standing> cmp = null;
        Map<UUID, int[]> h2h = null;
        for (String raw : tieBreak) {
            String key = raw == null ? "" : raw.toUpperCase(Locale.ROOT);
            Comparator<Standing> c = null;
            switch (key) {
                case "GOAL_DIFF" -> c = Comparator.comparingInt(Standing::getGoalDifference).reversed();
                case "GOALS_FOR" -> c = Comparator.comparingInt(Standing::getGoalsFor).reversed();
                case "GOALS_AGAINST" -> c = Comparator.comparingInt(Standing::getGoalsAgainst); // fewer is better
                case "WINS" -> c = Comparator.comparingInt(Standing::getWon).reversed();
                case "HEAD_TO_HEAD" -> {
                    if (h2h == null) h2h = headToHead(run, matches, pWin, pDraw);
                    final Map<UUID, int[]> hh = h2h;
                    c = Comparator
                        .comparingInt((Standing s) -> hh.getOrDefault(s.getTeam().getId(), ZERO3)[0]).reversed()
                        .thenComparing(Comparator.comparingInt((Standing s) -> hh.getOrDefault(s.getTeam().getId(), ZERO3)[1]).reversed())
                        .thenComparing(Comparator.comparingInt((Standing s) -> hh.getOrDefault(s.getTeam().getId(), ZERO3)[2]).reversed());
                }
                default -> { /* FAIR_PLAY, DRAW_LOTS: no data, skip */ }
            }
            if (c != null) cmp = (cmp == null) ? c : cmp.thenComparing(c);
        }
        return cmp != null ? cmp : (a, b) -> 0;
    }

    /** Mini-table [points, goalDiff, goalsFor] among only the tied teams. */
    private Map<UUID, int[]> headToHead(List<Standing> run, List<Match> matches, int pWin, int pDraw) {
        Set<UUID> ids = new HashSet<>();
        for (Standing s : run) ids.add(s.getTeam().getId());
        Map<UUID, int[]> map = new HashMap<>();
        for (UUID id : ids) map.put(id, new int[]{0, 0, 0});
        for (Match m : matches) {
            if (m.getHomeTeam() == null || m.getAwayTeam() == null) continue;
            if (m.getHomeScore() == null || m.getAwayScore() == null) continue;
            UUID h = m.getHomeTeam().getId(), a = m.getAwayTeam().getId();
            if (!ids.contains(h) || !ids.contains(a)) continue;
            int hs = m.getHomeScore(), as = m.getAwayScore();
            int[] hh = map.get(h), aa = map.get(a);
            hh[2] += hs; aa[2] += as;
            hh[1] += (hs - as); aa[1] += (as - hs);
            if (hs > as) hh[0] += pWin;
            else if (hs < as) aa[0] += pWin;
            else { hh[0] += pDraw; aa[0] += pDraw; }
        }
        return map;
    }
}
