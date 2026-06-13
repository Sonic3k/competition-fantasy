package com.fantasy.competition.service;

import com.fantasy.competition.entity.*;
import com.fantasy.competition.format.FormatPresetRegistry;
import com.fantasy.competition.format.TournamentFormat;
import com.fantasy.competition.format.TournamentFormat.Phase;
import com.fantasy.competition.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Builds a full tournament structure for a season from a TournamentFormat:
 *   - GROUP phases  -> a GROUP stage with N groups; if teams are supplied,
 *                      round-robin fixtures (concrete) are generated
 *   - KNOCKOUT phases -> a KNOCKOUT stage of TBD matches whose participants
 *                        are wired via match_slot (group positions / best
 *                        thirds for the first round, match winners afterwards)
 *
 * The advance engine (KnockoutAdvancementService) later fills the TBD slots.
 *
 * NOTE: first-round seeding is a structurally-valid default (winners paired
 * against runners-up/thirds, top-vs-bottom). Official per-tournament seeding
 * and best-third same-group avoidance can be encoded per bracketMap later.
 */
@Service
@RequiredArgsConstructor
public class TournamentGeneratorService {

    private final SeasonRepository seasonRepo;
    private final StageRepository stageRepo;
    private final StageGroupRepository stageGroupRepo;
    private final RoundRepository roundRepo;
    private final MatchRepository matchRepo;
    private final MatchSlotRepository matchSlotRepo;
    private final TeamRepository teamRepo;
    private final FormatPresetRegistry presets;
    private final ObjectMapper objectMapper;

    /** Describes where a knockout participant comes from (mirrors MatchSlot). */
    private static class SlotSpec {
        MatchSlot.SourceKind kind;
        StageGroup group;
        Integer position;
        Match sourceMatch;
        Integer bestThirdRank;
        String label;
    }

    @Transactional
    public void generate(UUID seasonId, String presetKey, List<UUID> teamIds) {
        Season season = seasonRepo.findById(seasonId).orElseThrow();
        if (season.getStatus() == Season.SeasonStatus.COMPLETED) {
            throw new IllegalStateException("Season is completed; cannot generate structure.");
        }
        if (!stageRepo.findBySeasonIdOrderByOrderNumber(seasonId).isEmpty()) {
            throw new IllegalStateException("Season already has stages. Delete the existing structure before generating.");
        }

        TournamentFormat fmt = (presetKey != null) ? presets.get(presetKey) : resolveFormat(season);
        if (fmt == null) throw new IllegalArgumentException("Unknown format preset: " + presetKey);

        // Snapshot the format onto the season.
        season.setFormatPresetKey(fmt.presetKey);
        try {
            season.setFormatConfig(objectMapper.writeValueAsString(fmt));
        } catch (Exception ignored) { /* config snapshot is best-effort */ }
        seasonRepo.save(season);

        List<Team> drawTeams = resolveTeams(season, teamIds);

        int stageOrder = 1;
        int koRoundNo = 1;
        List<SlotSpec> koFeed = null;          // qualifier specs from the last group phase
        List<Match> prevCanonical = null;      // canonical match per pairing of the previous KO round
        boolean firstKoConsumed = false;

        for (Phase phase : fmt.phases) {
            if ("GROUP".equalsIgnoreCase(phase.type)) {
                List<StageGroup> groups = buildGroupPhase(season, phase, stageOrder++, drawTeams);
                koFeed = qualifierSpecs(groups, phase);   // last group phase wins as the KO feed
            } else if ("KNOCKOUT".equalsIgnoreCase(phase.type)) {
                Stage stage = newStage(season, phase.name, Stage.StageType.KNOCKOUT, stageOrder++, nz(phase.legs, 1));
                Round round = newRound(season, stage, null, koRoundNo++, phase.name);

                // Determine this round's matchups.
                List<SlotSpec[]> matchups = new ArrayList<>();
                if (!firstKoConsumed) {
                    List<SlotSpec> entrants = (koFeed != null) ? koFeed : new ArrayList<>();
                    int size = entrants.size();
                    for (int i = 0; i < size / 2; i++) {
                        matchups.add(new SlotSpec[]{entrants.get(i), entrants.get(size - 1 - i)});
                    }
                    firstKoConsumed = true;
                } else {
                    List<Match> prev = (prevCanonical != null) ? prevCanonical : new ArrayList<>();
                    for (int i = 0; i + 1 < prev.size(); i += 2) {
                        matchups.add(new SlotSpec[]{
                            winnerSpec(prev.get(i)), winnerSpec(prev.get(i + 1))
                        });
                    }
                }

                List<Match> canonical = new ArrayList<>();
                for (SlotSpec[] pair : matchups) {
                    canonical.add(createKnockoutTie(round, pair[0], pair[1], nz(phase.legs, 1)));
                }

                // Third-place play-off: losers of the two semi-finals.
                if (fmt.thirdPlacePlayoff && phase.size != null && phase.size == 2
                        && prevCanonical != null && prevCanonical.size() == 2) {
                    Round tpRound = newRound(season, stage, null, koRoundNo++, "Third-place play-off");
                    createKnockoutTie(tpRound,
                        loserSpec(prevCanonical.get(0)), loserSpec(prevCanonical.get(1)), 1);
                }

                prevCanonical = canonical;
            }
        }
    }

    // ---- group phase ----

    private List<StageGroup> buildGroupPhase(Season season, Phase phase, int order, List<Team> drawTeams) {
        int numGroups = nz(phase.numGroups, 1);
        Stage stage = newStage(season, phase.name, Stage.StageType.GROUP, order, nz(phase.legs, 1));

        List<StageGroup> groups = new ArrayList<>();
        for (int g = 0; g < numGroups; g++) {
            StageGroup grp = new StageGroup();
            grp.setStage(stage);
            grp.setName(numGroups == 1 ? phase.name : "Group " + (char) ('A' + g));
            groups.add(stageGroupRepo.save(grp));
        }

        // Draw teams into groups only if the count fits the configuration.
        Integer tpg = phase.teamsPerGroup;
        boolean fits = !drawTeams.isEmpty()
            && (tpg == null || drawTeams.size() == numGroups * tpg);
        if (fits) {
            List<List<Team>> buckets = new ArrayList<>();
            for (int g = 0; g < numGroups; g++) buckets.add(new ArrayList<>());
            for (int i = 0; i < drawTeams.size(); i++) buckets.get(i % numGroups).add(drawTeams.get(i));
            for (int g = 0; g < numGroups; g++) {
                StageGroup grp = groups.get(g);
                grp.getTeams().addAll(buckets.get(g));
                stageGroupRepo.save(grp);
                generateRoundRobin(season, stage, grp, buckets.get(g), nz(phase.legs, 1));
            }
        }
        return groups;
    }

    /** Circle-method round-robin; legs=2 adds a reversed-venue second cycle. */
    private void generateRoundRobin(Season season, Stage stage, StageGroup group, List<Team> teams, int legs) {
        if (teams.size() < 2) return;
        int[] roundNo = {1};
        scheduleLeg(season, stage, group, teams, 1, roundNo);
        if (legs >= 2) scheduleLeg(season, stage, group, teams, 2, roundNo);
    }

    private void scheduleLeg(Season season, Stage stage, StageGroup group, List<Team> teams,
                             int leg, int[] roundNo) {
        List<Team> arr = new ArrayList<>(teams);
        if (arr.size() % 2 != 0) arr.add(null); // bye marker
        int m = arr.size();
        int rounds = m - 1;
        int half = m / 2;
        for (int r = 0; r < rounds; r++) {
            int rn = roundNo[0]++;
            Round round = newRound(season, stage, group, rn, "Round " + rn);
            for (int i = 0; i < half; i++) {
                Team a = arr.get(i);
                Team b = arr.get(m - 1 - i);
                if (a == null || b == null) continue;
                if (leg == 2) createMatch(round, b, a, 2); else createMatch(round, a, b, 1);
            }
            arr.add(1, arr.remove(m - 1)); // rotate, keep arr[0] fixed
        }
    }

    /** Top-N positions of each group + best thirds, as KO feed specs. */
    private List<SlotSpec> qualifierSpecs(List<StageGroup> groups, Phase phase) {
        int advance = nz(phase.advancePerGroup, 0);
        int bestThirds = nz(phase.bestThirds, 0);
        List<SlotSpec> feed = new ArrayList<>();
        // winners first, then runners-up, then 3rd, ... (spreads seeds across the bracket)
        for (int pos = 1; pos <= advance; pos++) {
            for (int g = 0; g < groups.size(); g++) {
                SlotSpec s = new SlotSpec();
                s.kind = MatchSlot.SourceKind.GROUP_POSITION;
                s.group = groups.get(g);
                s.position = pos;
                s.label = positionName(pos) + " " + groups.get(g).getName();
                feed.add(s);
            }
        }
        for (int k = 1; k <= bestThirds; k++) {
            SlotSpec s = new SlotSpec();
            s.kind = MatchSlot.SourceKind.BEST_THIRD;
            s.bestThirdRank = k;
            s.label = "Best 3rd #" + k;
            feed.add(s);
        }
        return feed;
    }

    // ---- knockout tie creation ----

    /** Creates one knockout tie (1 match, or 2 legs sharing a tie_id) and returns the canonical match. */
    private Match createKnockoutTie(Round round, SlotSpec home, SlotSpec away, int legs) {
        if (legs >= 2) {
            UUID tieId = UUID.randomUUID();
            Match leg1 = newTbdMatch(round, 1, tieId);
            addSlot(leg1, MatchSlot.Side.HOME, home);
            addSlot(leg1, MatchSlot.Side.AWAY, away);
            Match leg2 = newTbdMatch(round, 2, tieId);
            addSlot(leg2, MatchSlot.Side.HOME, away); // venues reversed
            addSlot(leg2, MatchSlot.Side.AWAY, home);
            return leg2; // leg 2 is the decider / canonical result holder
        }
        Match m = newTbdMatch(round, null, null);
        addSlot(m, MatchSlot.Side.HOME, home);
        addSlot(m, MatchSlot.Side.AWAY, away);
        return m;
    }

    private SlotSpec winnerSpec(Match canonical) {
        SlotSpec s = new SlotSpec();
        s.kind = MatchSlot.SourceKind.MATCH_WINNER;
        s.sourceMatch = canonical;
        s.label = "Winner of " + (canonical.getRound() != null ? canonical.getRound().getName() : "previous round");
        return s;
    }

    private SlotSpec loserSpec(Match canonical) {
        SlotSpec s = new SlotSpec();
        s.kind = MatchSlot.SourceKind.MATCH_LOSER;
        s.sourceMatch = canonical;
        s.label = "Loser of " + (canonical.getRound() != null ? canonical.getRound().getName() : "semi-final");
        return s;
    }

    // ---- low-level builders ----

    private Stage newStage(Season season, String name, Stage.StageType type, int order, int legs) {
        Stage s = new Stage();
        s.setSeason(season);
        s.setName(name);
        s.setType(type);
        s.setOrderNumber(order);
        s.setLegs(legs);
        return stageRepo.save(s);
    }

    private Round newRound(Season season, Stage stage, StageGroup group, int roundNumber, String name) {
        Round r = new Round();
        r.setSeason(season);
        r.setStage(stage);
        r.setStageGroup(group);
        r.setRoundNumber(roundNumber);
        r.setName(name);
        return roundRepo.save(r);
    }

    private Match createMatch(Round round, Team home, Team away, Integer leg) {
        Match m = new Match();
        m.setRound(round);
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setLeg(leg);
        m.setStatus(Match.MatchStatus.SCHEDULED);
        return matchRepo.save(m);
    }

    private Match newTbdMatch(Round round, Integer leg, UUID tieId) {
        Match m = new Match();
        m.setRound(round);
        m.setLeg(leg);
        m.setTieId(tieId);
        m.setStatus(Match.MatchStatus.SCHEDULED);
        return matchRepo.save(m);
    }

    private void addSlot(Match match, MatchSlot.Side side, SlotSpec spec) {
        MatchSlot slot = new MatchSlot();
        slot.setMatch(match);
        slot.setSide(side);
        slot.setSourceKind(spec.kind);
        slot.setSourceGroup(spec.group);
        slot.setSourcePosition(spec.position);
        slot.setSourceMatch(spec.sourceMatch);
        slot.setBestThirdRank(spec.bestThirdRank);
        slot.setLabel(spec.label);
        matchSlotRepo.save(slot);
    }

    // ---- helpers ----

    private List<Team> resolveTeams(Season season, List<UUID> teamIds) {
        if (teamIds != null && !teamIds.isEmpty()) {
            List<Team> out = new ArrayList<>();
            for (UUID id : teamIds) teamRepo.findById(id).ifPresent(out::add);
            return out;
        }
        return season.getTeams() != null ? new ArrayList<>(season.getTeams()) : new ArrayList<>();
    }

    private TournamentFormat resolveFormat(Season season) {
        if (season.getFormatConfig() != null && !season.getFormatConfig().isBlank()) {
            try {
                return objectMapper.readValue(season.getFormatConfig(), TournamentFormat.class);
            } catch (Exception ignored) { /* fall back to preset */ }
        }
        return presets.get(season.getFormatPresetKey());
    }

    private static String positionName(int pos) {
        return switch (pos) {
            case 1 -> "Winner";
            case 2 -> "Runner-up";
            case 3 -> "3rd";
            default -> pos + "th";
        };
    }

    private static int nz(Integer v, int dflt) {
        return v != null ? v : dflt;
    }
}
