package com.arsw.bomberman.observability;

import com.arsw.bomberman.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RoomEventLoggerSuscriber{

    private static final Logger logger = LoggerFactory.getLogger("domain-events");

    @EventListener
    public void onRoomCreated(RoomCreatedEvent event){
        logger.info("Sala creada roomId={} name={} capacitiy={} createdBy={}", event.roomId(), event.roomName(), event.capacity(), event.createdBy());
    }

    @EventListener
    public void onRoomJoined(RoomJoinedEvent event){
        logger.info("Jugador unido a sala roomId={} userId={} username={} players={}", event.roomId(), event.userId(), event.username(), event.currentPlayers());
    }

    @EventListener
    public void onRoomError(RoomErrorEvent event) {
        logger.warn("Error en sala roomId={} userId={} reason={}", event.roomId(), event.userId(), event.reason());
    }

    @EventListener
    public void onGameStarted(GameStartedEvent event) {
        logger.info("Partida iniciada roomId={} players={}", event.roomId(), event.playerIds());
    }
}