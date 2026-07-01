package com.arsw.bomberman.observability;

import com.arsw.bomberman.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuthEventLoggerSubscriber {

    private static final Logger logger = LoggerFactory.getLogger("domain-events");

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        logger.info("Usuario registrado userId={} username={}", event.userId(), event.username());
    }

    @EventListener
    public void onUserRegistrationFailed(UserRegistrationFailedEvent event) {
        logger.warn("Registro fallido username={} reason={}", event.username(), event.reason());
    }

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        logger.info("Login exitoso userId={} username={}", event.userId(), event.username());
    }

    @EventListener
    public void onUserLoginFailed(UserLoginFailedEvent event) {
        logger.warn("Login fallido username={} reason={}", event.username(), event.reason());
    }
}