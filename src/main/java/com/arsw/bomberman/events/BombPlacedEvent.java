package com.arsw.bomberman.events;

import java.time.Instant;

public record BombPlacedEvent(String roomId, Long userId, int x, int y, Instant occurredAt){

    public BombPlacedEvent(String roomId, Long userId, int x, int y) {
        this(roomId, userId, x, y, Instant.now());
    }
}