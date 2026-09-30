package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TeamAliasRepository extends JpaRepository<TeamAlias, Long> {
    List<TeamAlias> findByTeamId(Long teamId);
    List<TeamAlias> findByTeamIdIn(Collection<Long> teamIds);
    void deleteByTeamId(Long teamId);

    @Query("select a from TeamAlias a where a.teamId in (select t.id from Team t where t.universeId = :universeId)")
    List<TeamAlias> findByUniverseId(@Param("universeId") Long universeId);
}
