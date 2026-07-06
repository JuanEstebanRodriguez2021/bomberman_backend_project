package com.arsw.bomberman.events;

import java.time.Instant;

public record PlayerEliminatedEvent(String roomId, Long userId, Instant occurredAt){
    
    public PlayerEliminatedEvent(String roomId, Long userId) {
        this(roomId, userId, Instant.now());
    }
}