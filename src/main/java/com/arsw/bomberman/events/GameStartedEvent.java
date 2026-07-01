package com.arsw.bomberman.events;

import java.time.Instant;
import java.util.List;

public record GameStartedEvent(String roomId, List<Long> playerIds, Instant ocurredAt){

    public GameStartedEvent(String roomId, List <Long> playerIds ){
        this(roomId, playerIds, Instant.now());
    }
}