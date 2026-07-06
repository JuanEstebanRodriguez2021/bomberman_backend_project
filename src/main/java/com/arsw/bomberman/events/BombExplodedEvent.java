package com.arsw.bomberman.events;

import java.time.Instant;
import java.util.List;

public record BombExplodedEvent(String roomId, int x, int y, List<int[]> affectedCells, List<Long> eliminatedPlayers, Instant occurredAt){
    
    public BombExplodedEvent(String roomId, int x, int y, List<int[]> affectedCells, List<Long> eliminatedPlayers) {
        this(roomId, x, y, affectedCells, eliminatedPlayers, Instant.now());
    }
}