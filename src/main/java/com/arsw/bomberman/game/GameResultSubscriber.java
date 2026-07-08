package com.arsw.bomberman.game;

import com.arsw.bomberman.events.GameFinishedEvent;
import com.arsw.bomberman.events.GameStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameResultSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(GameResultSubscriber.class);

    private final GameResultRepository repository;

    private final Map<String, Instant> startTimes = new ConcurrentHashMap<>();

    public GameResultSubscriber(GameResultRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onGameStarted(GameStartedEvent event) {
        startTimes.put(event.roomId(), event.ocurredAt());
    }

    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        try {
            Instant startedAt = startTimes.getOrDefault(event.roomId(), event.occurredAt());
            startTimes.remove(event.roomId());

            GameResult result = new GameResult(
                    event.roomId(),
                    event.winnerId(),
                    event.winnerUsername(),
                    0,
                    startedAt
            );

            repository.save(result);
            logger.info("Resultado de partida persistido roomId={} winnerId={}",
                    event.roomId(), event.winnerId());

        } catch (Exception e) {
            logger.error("Error al persistir resultado de partida roomId={} error={}",
                    event.roomId(), e.getMessage());
        }
    }
}