package com.arsw.bomberman.sockets;

import com.arsw.bomberman.auth.JwtService;
import com.arsw.bomberman.game.GameEngine;
import com.arsw.bomberman.game.GameState;
import com.arsw.bomberman.rooms.Room;
import com.arsw.bomberman.rooms.RoomManager;
import com.arsw.bomberman.rooms.RoomResponse;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import com.arsw.bomberman.events.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SocketGateway {

    private static final Logger logger = LoggerFactory.getLogger(SocketGateway.class);

    private final SocketIOServer server;
    private final JwtService jwtService;
    private final RoomManager roomManager;
    private final GameEngine gameEngine;

    public SocketGateway(@Nullable SocketIOServer server, JwtService jwtService, RoomManager roomManager, GameEngine gameEngine) {
        this.server = server;
        this.jwtService = jwtService;
        this.roomManager = roomManager;
        this.gameEngine = gameEngine;
    }

    @PostConstruct
    public void registerHandlers() {
        if (server == null) {
            logger.info("Socket.IO deshabilitado (SOCKETIO_ENABLED=false)");
            return;
        }

        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);


        server.addEventListener("room:create", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            if (userId == null) { client.sendEvent("room:error", Map.of("message", "No autenticado")); return; }

            String name = (String) data.getOrDefault("name", "Sala sin nombre");
            int capacity = (int) data.getOrDefault("capacity", 4);
            Room room = roomManager.createRoom(name, capacity, userId);
            server.getBroadcastOperations().sendEvent("room:created", RoomResponse.from(room));
        });

        server.addEventListener("room:list", Object.class, (client, data, ack) -> {
            List<RoomResponse> rooms = roomManager.listRooms().stream().map(RoomResponse::from).toList();
            client.sendEvent("room:list", rooms);
        });

        server.addEventListener("room:join", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            String username = client.get("username");
            String roomId = (String) data.get("roomId");

            if (userId == null || roomId == null) { client.sendEvent("room:error", Map.of("message", "Datos inválidos")); return; }

            RoomManager.JoinResult result = roomManager.joinRoom(roomId, userId, username, client.getSessionId().toString());

            if (!result.success()) { client.sendEvent("room:error", Map.of("message", result.error())); return; }

            client.joinRoom(roomId);
            client.set("roomId", roomId);

            server.getRoomOperations(roomId).sendEvent("player:joined", Map.of(
                    "roomId", roomId,
                    "player", Map.of("userId", userId, "username", username),
                    "room", RoomResponse.from(result.room())
            ));

            if (result.gameStarted()) {
                GameState state = gameEngine.initGame(roomId, result.room().getPlayers());

                Map<String, Object> initialState = buildGameStatePayload(state);

                server.getRoomOperations(roomId).sendEvent("game:start", Map.of(
                        "roomId", roomId,
                        "startedAt", System.currentTimeMillis(),
                        "state", initialState
                ));
                logger.info("game:start emitido roomId={}", roomId);
                return;
            }

            if (result.reconnected()) {
                gameEngine.getState(roomId).ifPresent(state -> {
                    Map<String, Object> currentState = buildGameStatePayload(state);
                    client.sendEvent("game:start", Map.of(
                            "roomId", roomId,
                            "startedAt", System.currentTimeMillis(),
                            "state", currentState
                    ));
                    logger.info("game:start (resync) emitido a userId={} roomId={}", userId, roomId);
                });
            }
        });

        server.addEventListener("player:move", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            String roomId = client.get("roomId");
            String direction = (String) data.getOrDefault("direction", "");
            long clientTimestamp = ((Number) data.getOrDefault("timestamp", System.currentTimeMillis())).longValue();

            if (userId == null || roomId == null) return;

            GameEngine.MoveResult result = gameEngine.movePlayer(roomId, userId, direction, clientTimestamp);
            if (!result.valid()) return;
            server.getRoomOperations(roomId).sendEvent("player:position", Map.of(
                    "userId", userId,
                    "x", result.x(),
                    "y", result.y()
            ));
        });

        server.addEventListener("bomb:place", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            String roomId = client.get("roomId");

            if (userId == null || roomId == null) return;

            GameEngine.BombResult result = gameEngine.placeBomb(roomId, userId);
            if (!result.valid()) { client.sendEvent("bomb:error", Map.of("message", result.error())); return; }

            server.getRoomOperations(roomId).sendEvent("bomb:placed", Map.of(
                    "userId", userId,
                    "x", result.x(),
                    "y", result.y(),
                    "timer", 3
            ));
        });
    }

    @EventListener
    public void onBombExploded(BombExplodedEvent event) {
        if (server == null) return;

        List<Map<String, Integer>> cells = event.affectedCells().stream()
                .map(c -> Map.of("x", c[0], "y", c[1]))
                .collect(Collectors.toList());

        server.getRoomOperations(event.roomId()).sendEvent("bomb:explode", Map.of(
                "x", event.x(),
                "y", event.y(),
                "cells", cells,
                "eliminated", event.eliminatedPlayers()
        ));
    }

    @EventListener
    public void onPlayerEliminated(PlayerEliminatedEvent event) {
        if (server == null) return;
        server.getRoomOperations(event.roomId()).sendEvent("player:eliminated", Map.of(
                "userId", event.userId()
        ));
    }

    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        if (server == null) return;
        server.getRoomOperations(event.roomId()).sendEvent("game:over", Map.of(
                "winnerId", event.winnerId() != null ? event.winnerId() : -1,
                "winnerUsername", event.winnerUsername() != null ? event.winnerUsername() : "Empate"
        ));
        gameEngine.removeGame(event.roomId());
        roomManager.removeRoom(event.roomId());
    }

    private Map<String, Object> buildGameStatePayload(GameState state) {
        List<Map<String, Object>> players = state.getPlayers().values().stream()
                .map(p -> Map.<String, Object>of(
                        "userId", p.getUserId(),
                        "username", p.getUsername(),
                        "x", p.getX(),
                        "y", p.getY(),
                        "alive", p.isAlive()
                ))
                .toList();

        List<Map<String, Object>> bombs = state.getActiveBombs().stream()
                .map(b -> Map.<String, Object>of(
                        "userId", b.userId(),
                        "x", b.x(),
                        "y", b.y(),
                        "timer", 3
                ))
                .toList();

        return Map.of(
                "map", state.getMap().getGrid(),
                "players", players,
                "bombs", bombs
        );
    }

    private void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        if (token == null || !jwtService.isValid(token)) {
            logger.warn("Conexión rechazada: token inválido sessionId={}", client.getSessionId());
            client.disconnect();
            return;
        }
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        client.set("userId", userId);
        client.set("username", username);
        logger.info("Cliente conectado userId={} sessionId={}", userId, client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        Long userId = client.get("userId");
        String roomId = client.get("roomId");
        logger.info("Cliente desconectado userId={} sessionId={}", userId, client.getSessionId());

        if (roomId != null) {
            roomManager.removePlayer(client.getSessionId().toString());
            server.getRoomOperations(roomId).sendEvent("player:left", Map.of( "userId", userId != null ? userId : -1,"roomId", roomId));
        }
    }
}