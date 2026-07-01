package com.arsw.bomberman.events;

import java.time.Instant;

public record UserLoginFailedEvent(String username, String reason, Instant occurredAt) {
    public UserLoginFailedEvent(String username, String reason) {
        this(username, reason, Instant.now());
    }
}