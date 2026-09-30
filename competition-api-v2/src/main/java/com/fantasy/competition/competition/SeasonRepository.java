package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findByCompetitionIdOrderByYearDescNameDesc(Long competitionId);
    Optional<Season> findByCompetitionIdAndKey(Long competitionId, String key);
    List<Season> findByCompetitionIdIn(Collection<Long> competitionIds);

    @Query("select s from Season s where s.id in (select st.seasonId from SeasonTeam st where st.teamId = :teamId) order by s.year desc, s.id desc")
    List<Season> findByTeamId(@Param("teamId") Long teamId);
}
