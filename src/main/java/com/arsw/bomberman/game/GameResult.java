package com.arsw.bomberman.game;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "game_results")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "winner_username")
    private String winnerUsername;

    @Column(name = "player_count")
    private int playerCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt = Instant.now();

    public GameResult() {}

    public GameResult(String roomId, Long winnerId, String winnerUsername,
                      int playerCount, Instant startedAt) {
        this.roomId = roomId;
        this.winnerId = winnerId;
        this.winnerUsername = winnerUsername;
        this.playerCount = playerCount;
        this.startedAt = startedAt;
    }

    public Long getId() { return id; }
    public String getRoomId() { return roomId; }
    public Long getWinnerId() { return winnerId; }
    public String getWinnerUsername() { return winnerUsername; }
    public int getPlayerCount() { return playerCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
}