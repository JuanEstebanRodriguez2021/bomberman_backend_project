package com.arsw.bomberman.events;

import java.time.Instant;

public record PlayerMovedEvent(String roomId, Long userId, int x, int y, long latencyMs, Instant ocurredAt){

    public PlayerMovedEvent(String roomId, Long userId, int x, int y, long latencyMs){
        this(roomId, userId, x, y, latencyMs, Instant.now());
    }

}