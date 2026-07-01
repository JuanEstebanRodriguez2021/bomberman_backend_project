package com.arsw.bomberman.events;

import java.time.Instant;

public record UserRegistrationFailedEvent(String username, String reason, Instant occurredAt) {
    public UserRegistrationFailedEvent(String username, String reason) {
        this(username, reason, Instant.now());
    }
}