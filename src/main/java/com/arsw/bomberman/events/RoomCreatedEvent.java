package com.arsw.bomberman.events;

import java.time.Instant;

public record RoomCreatedEvent(String roomId, String roomName, int capacity, Long createdBy, Instant ocurredAt){

    public RoomCreatedEvent(String roomId, String roomName, int capacity, Long createdBy){
        this(roomId, roomName, capacity, createdBy, Instant.now());
    }
}