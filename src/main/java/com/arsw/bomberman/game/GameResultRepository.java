package com.arsw.bomberman.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    List<GameResult> findByWinnerIdOrderByFinishedAtDesc(Long winnerId);

    @Query("SELECT COUNT(g) FROM GameResult g WHERE g.finishedAt >= :since")
    long countSince(java.time.Instant since);
}