package com.arsw.bomberman.events;

import java.time.Instant;

public record RoomJoinedEvent(String roomId, Long userId, String username, int currentPlayers, Instant ocurredAt){

    public RoomJoinedEvent(String roomId, Long userId, String username, int currentPlayers){
        this(roomId, userId, username, currentPlayers, Instant.now());
    }
}