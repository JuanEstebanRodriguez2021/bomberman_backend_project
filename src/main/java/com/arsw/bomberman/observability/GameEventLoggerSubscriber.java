package com.arsw.bomberman.observability;

import com.arsw.bomberman.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GameEventLoggerSubscriber {

    private static final Logger logger = LoggerFactory.getLogger("domain-events");

    @EventListener
    public void onPlayerMoved(PlayerMovedEvent event) {
        logger.info("player:move roomId={} userId={} x={} y={} latencyMs={}",
            event.roomId(), event.userId(), event.x(), event.y(), event.latencyMs());
    }

    @EventListener
    public void onBombPlaced(BombPlacedEvent event) {
        logger.info("bomb:place roomId={} userId={} x={} y={}",
            event.roomId(), event.userId(), event.x(), event.y());
    }

    @EventListener
    public void onBombExploded(BombExplodedEvent event) {
        logger.info("bomb:explode roomId={} x={} y={} eliminated={}",
            event.roomId(), event.x(), event.y(), event.eliminatedPlayers());
    }

    @EventListener
    public void onPlayerEliminated(PlayerEliminatedEvent event) {
        logger.warn("player:eliminated roomId={} userId={}",
            event.roomId(), event.userId());
    }

    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        logger.info("game:finished roomId={} winnerId={} winner={}",
            event.roomId(), event.winnerId(), event.winnerUsername());
    }
}