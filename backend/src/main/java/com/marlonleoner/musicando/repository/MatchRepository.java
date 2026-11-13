package com.marlonleoner.musicando.repository;

import com.marlonleoner.musicando.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {

}
