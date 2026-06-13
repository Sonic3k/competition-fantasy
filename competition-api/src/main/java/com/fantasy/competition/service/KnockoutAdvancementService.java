package com.fantasy.competition.service;

import com.fantasy.competition.dto.AdvanceResult;
import com.fantasy.competition.entity.*;
import com.fantasy.competition.format.FormatPresetRegistry;
import com.fantasy.competition.format.TournamentFormat;
import com.fantasy.competition.format.TournamentFormat.Phase;
import com.fantasy.competition.repository.MatchRepository;
import com.fantasy.competition.repository.MatchSlotRepository;
import com.fantasy.competition.repository.SeasonRepository;
import com.fantasy.competition.repository.StandingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Semi-automatic tournament progression (the "advance" button). Idempotent:
 * each call fills whatever is now resolvable and propagates completed results;
 * re-running after more scores are entered advances the next layer.
 *
 * Steps per call:
 *   1. recompute group standings (per group, configured tie-break)
 *   2. resolve group qualifiers (top-N) + best-third ranking
 *   3. resolve outcomes of completed knockout ties (single + two-leg aggregate)
 *   4. fill TBD match slots (group positions / best thirds / match winners-losers)
 *
 * Guard: disabled when the season is COMPLETED (history).
 */
@Service
@RequiredArgsConstructor
public class KnockoutAdvancementService {

    private final SeasonRepository seasonRepo;
    private final MatchRepository matchRepo;
    private final StandingRepository standingRepo;
    private final MatchSlotRepository matchSlotRepo;
    private final StandingService standingService;
    private final FormatPresetRegistry presets;
    private final ObjectMapper objectMapper;

    @Transactional
    public AdvanceResult advance(UUID seasonId) {
        Season season = seasonRepo.findById(seasonId).orElseThrow();
        if (season.getStatus() == Season.SeasonStatus.COMPLETED) {
            return new AdvanceResult(0, 0, false, "Season is completed — advance is disabled.");
        }

        // 1. Recompute group standings.
        standingService.recalculate(seasonId, null);

        TournamentFormat fmt = resolveFormat(season);
        int bestThirdsNeeded = 0;
        if (fmt != null) {
            Phase gp = fmt.firstGroupPhase();
            if (gp != null && gp.bestThirds != null) bestThirdsNeeded = gp.bestThirds;
        }
        List<String> koDeciders = (fmt != null && fmt.koDeciders != null && !fmt.koDeciders.isEmpty())
            ? fmt.koDeciders : List.of("PENALTIES");

        List<Match> all = matchRepo.findByRoundSeasonId(seasonId);

        // 2. Group completion + (group,rank)->team + best-third ranking.
        Map<UUID, Boolean> groupComplete = computeGroupCompletion(all);
        boolean allGroupsComplete = !groupComplete.isEmpty()
            && groupComplete.values().stream().allMatch(Boolean::booleanValue);

        List<Standing> calc = standingRepo
            .findBySeasonIdAndTypeAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(seasonId, Standing.StandingType.CALCULATED);
        Map<String, Team> posTeam = new HashMap<>();
        List<Standing> thirds = new ArrayList<>();
        for (Standing s : calc) {
            if (s.getStageGroup() == null || s.getRank() == null || s.getTeam() == null) continue;
            posTeam.put(key(s.getStageGroup().getId(), s.getRank()), s.getTeam());
            if (s.getRank() == 3) thirds.add(s);
        }

        Map<Integer, Team> bestThirdTeam = new HashMap<>();
        if (bestThirdsNeeded > 0 && allGroupsComplete) {
            thirds.sort(Comparator
                .comparingInt(Standing::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(Standing::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(Standing::getGoalsFor).reversed()));
            for (int i = 0; i < Math.min(bestThirdsNeeded, thirds.size()); i++) {
                bestThirdTeam.put(i + 1, thirds.get(i).getTeam());
            }
        }

        // 3. Resolve outcomes of completed knockout ties.
        Map<UUID, List<Match>> byTie = new HashMap<>();
        for (Match m : all) {
            if (m.getTieId() != null) byTie.computeIfAbsent(m.getTieId(), k -> new ArrayList<>()).add(m);
        }
        Map<UUID, Team[]> outcome = new HashMap<>(); // canonical match id -> {winner, loser}
        for (Match m : all) {
            if (!isKnockout(m)) continue;
            Team[] wl = null;
            if (m.getTieId() == null) {
                wl = singleOutcome(m);
            } else if (m.getLeg() != null && m.getLeg() == 2) {
                wl = tieOutcome(byTie.get(m.getTieId()), koDeciders);
            }
            if (wl != null) {
                m.setWinnerTeam(wl[0]);
                matchRepo.save(m);
                outcome.put(m.getId(), wl);
            }
        }

        // 4. Fill TBD slots.
        int slotsFilled = 0;
        for (Match m : all) {
            if (!isKnockout(m)) continue;
            boolean dirty = false;
            for (MatchSlot s : matchSlotRepo.findByMatchId(m.getId())) {
                boolean isHome = s.getSide() == MatchSlot.Side.HOME;
                Team current = isHome ? m.getHomeTeam() : m.getAwayTeam();
                if (current != null) continue;
                Team t = resolveSlot(s, posTeam, groupComplete, bestThirdTeam, outcome);
                if (t != null) {
                    if (isHome) m.setHomeTeam(t); else m.setAwayTeam(t);
                    slotsFilled++;
                    dirty = true;
                }
            }
            if (dirty) matchRepo.save(m);
        }

        boolean finalDecided = all.stream().anyMatch(m ->
            m.getRound() != null && "Final".equalsIgnoreCase(m.getRound().getName()) && m.getWinnerTeam() != null);

        String msg = String.format(
            "Standings recomputed. %d slot(s) filled, %d knockout tie(s) resolved.%s%s",
            slotsFilled, outcome.size(),
            allGroupsComplete ? "" : " Group stage not complete yet (some qualifiers pending).",
            finalDecided ? " Final decided." : "");
        return new AdvanceResult(slotsFilled, outcome.size(), finalDecided, msg);
    }

    private Team resolveSlot(MatchSlot s, Map<String, Team> posTeam, Map<UUID, Boolean> groupComplete,
                             Map<Integer, Team> bestThirdTeam, Map<UUID, Team[]> outcome) {
        return switch (s.getSourceKind()) {
            case GROUP_POSITION -> {
                if (s.getSourceGroup() == null || s.getSourcePosition() == null) yield null;
                UUID gid = s.getSourceGroup().getId();
                if (!Boolean.TRUE.equals(groupComplete.get(gid))) yield null; // wait until group finished
                yield posTeam.get(key(gid, s.getSourcePosition()));
            }
            case BEST_THIRD -> s.getBestThirdRank() == null ? null : bestThirdTeam.get(s.getBestThirdRank());
            case MATCH_WINNER -> {
                Team[] wl = s.getSourceMatch() == null ? null : outcome.get(s.getSourceMatch().getId());
                yield wl == null ? null : wl[0];
            }
            case MATCH_LOSER -> {
                Team[] wl = s.getSourceMatch() == null ? null : outcome.get(s.getSourceMatch().getId());
                yield wl == null ? null : wl[1];
            }
        };
    }

    /** Single-leg knockout result: higher score, else penalties. Returns {winner, loser} or null. */
    private Team[] singleOutcome(Match m) {
        if (m.getHomeTeam() == null || m.getAwayTeam() == null) return null;
        if (m.getHomeScore() == null || m.getAwayScore() == null) return null;
        int h = m.getHomeScore(), a = m.getAwayScore();
        if (h != a) {
            // Decisive score — keep a manually-set method (e.g. EXTRA_TIME); default to REGULAR.
            if (m.getDecidedBy() == null) m.setDecidedBy(Match.DecidedBy.REGULAR);
            return h > a ? new Team[]{m.getHomeTeam(), m.getAwayTeam()}
                         : new Team[]{m.getAwayTeam(), m.getHomeTeam()};
        }
        Integer hp = m.getHomePenalties(), ap = m.getAwayPenalties();
        if (hp != null && ap != null && !hp.equals(ap)) {
            m.setDecidedBy(Match.DecidedBy.PENALTIES);
            return hp > ap ? new Team[]{m.getHomeTeam(), m.getAwayTeam()}
                           : new Team[]{m.getAwayTeam(), m.getHomeTeam()};
        }
        return null; // level with no shootout recorded
    }

    /**
     * Two-leg tie. Generator convention: leg1 home=A/away=B, leg2 home=B/away=A.
     * Aggregate, then optional away-goals, then penalties on leg 2. Winner is
     * recorded on leg 2 (the canonical match). Returns {winner, loser} or null.
     */
    private Team[] tieOutcome(List<Match> legs, List<String> deciders) {
        if (legs == null || legs.size() < 2) return null;
        Match leg1 = null, leg2 = null;
        for (Match m : legs) {
            if (m.getLeg() != null && m.getLeg() == 1) leg1 = m;
            else if (m.getLeg() != null && m.getLeg() == 2) leg2 = m;
        }
        if (leg1 == null || leg2 == null) return null;
        if (leg1.getHomeScore() == null || leg1.getAwayScore() == null
            || leg2.getHomeScore() == null || leg2.getAwayScore() == null) return null;
        Team a = leg1.getHomeTeam(), b = leg1.getAwayTeam();
        if (a == null || b == null) return null;

        int aggA = leg1.getHomeScore() + leg2.getAwayScore();
        int aggB = leg1.getAwayScore() + leg2.getHomeScore();
        if (aggA != aggB) {
            leg2.setDecidedBy(Match.DecidedBy.AGGREGATE);
            return aggA > aggB ? new Team[]{a, b} : new Team[]{b, a};
        }
        if (deciders.stream().anyMatch("AWAY_GOALS"::equalsIgnoreCase)) {
            int awayA = leg2.getAwayScore();   // A played away in leg 2
            int awayB = leg1.getAwayScore();   // B played away in leg 1
            if (awayA != awayB) {
                leg2.setDecidedBy(Match.DecidedBy.AGGREGATE);
                return awayA > awayB ? new Team[]{a, b} : new Team[]{b, a};
            }
        }
        Integer bp = leg2.getHomePenalties(); // B is home in leg 2
        Integer ap = leg2.getAwayPenalties(); // A is away in leg 2
        if (ap != null && bp != null && !ap.equals(bp)) {
            leg2.setDecidedBy(Match.DecidedBy.PENALTIES);
            return ap > bp ? new Team[]{a, b} : new Team[]{b, a};
        }
        return null;
    }

    private Map<UUID, Boolean> computeGroupCompletion(List<Match> all) {
        Map<UUID, List<Match>> byGroup = new HashMap<>();
        for (Match m : all) {
            Round r = m.getRound();
            if (r == null || r.getStageGroup() == null) continue;
            if (r.getStage() != null && r.getStage().getType() != Stage.StageType.GROUP) continue;
            byGroup.computeIfAbsent(r.getStageGroup().getId(), k -> new ArrayList<>()).add(m);
        }
        Map<UUID, Boolean> complete = new HashMap<>();
        for (Map.Entry<UUID, List<Match>> e : byGroup.entrySet()) {
            complete.put(e.getKey(),
                e.getValue().stream().allMatch(mm -> mm.getStatus() == Match.MatchStatus.COMPLETED));
        }
        return complete;
    }

    private boolean isKnockout(Match m) {
        Round r = m.getRound();
        return r != null && r.getStage() != null && r.getStage().getType() == Stage.StageType.KNOCKOUT;
    }

    private TournamentFormat resolveFormat(Season season) {
        if (season.getFormatConfig() != null && !season.getFormatConfig().isBlank()) {
            try {
                return objectMapper.readValue(season.getFormatConfig(), TournamentFormat.class);
            } catch (Exception ignored) { /* fall back to preset */ }
        }
        return presets.get(season.getFormatPresetKey());
    }

    private static String key(UUID groupId, int rank) {
        return groupId + "#" + rank;
    }
}
