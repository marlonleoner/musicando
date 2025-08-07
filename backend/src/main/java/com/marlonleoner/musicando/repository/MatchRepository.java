package com.marlonleoner.musicando.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marlonleoner.musicando.domain.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {

}