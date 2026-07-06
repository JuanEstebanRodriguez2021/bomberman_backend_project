package com.arsw.bomberman.events;

import java.time.Instant;

public record GameFinishedEvent(String roomId, Long winnerId, String winnerUsername, Instant occurredAt){
    
    public GameFinishedEvent(String roomId, Long winnerId, String winnerUsername) {
        this(roomId, winnerId, winnerUsername, Instant.now());
    }
}