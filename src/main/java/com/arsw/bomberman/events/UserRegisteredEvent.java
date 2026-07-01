package com.arsw.bomberman.events;

import java.time.Instant;

public record UserRegisteredEvent(Long userId, String username, Instant ocurredAt){

    public UserRegisteredEvent(Long userId, String username){
        this(userId,username,Instant.now());
    }
}