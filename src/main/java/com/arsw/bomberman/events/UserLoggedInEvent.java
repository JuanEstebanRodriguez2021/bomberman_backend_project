package com.arsw.bomberman.events;

import java.time.Instant;

public record UserLoggedInEvent(Long userId, String username, Instant occurredAt) {
    public UserLoggedInEvent(Long userId, String username) {
        this(userId, username, Instant.now());
    }
}