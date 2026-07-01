package com.arsw.bomberman.events;

import java.time.Instant;

public record RoomErrorEvent(String roomId, Long userId, String reason, Instant ocurredAt){

    public RoomErrorEvent(String roomId, Long userId, String reason){
        this(roomId, userId, reason, Instant.now());
    }
}