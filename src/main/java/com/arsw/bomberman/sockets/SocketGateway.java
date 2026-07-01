package com.arsw.bomberman.sockets;

import com.arsw.bomberman.rooms.RoomManager;
import com.arsw.bomberman.rooms.Room;
import com.arsw.bomberman.rooms.RoomResponse;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.arsw.bomberman.auth.JwtService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class SocketGateway {

    private static final Logger logger = LoggerFactory.getLogger(SocketGateway.class);

    private final SocketIOServer server;
    private final JwtService jwtService;
    private final RoomManager roomManager;

    public SocketGateway(@Nullable SocketIOServer server, JwtService jwtService, RoomManager roomManager) {
        this.server = server;
        this.jwtService = jwtService;
        this.roomManager = roomManager;
    }

    @PostConstruct
    public void registerHandlers() {
        if (server == null) {
            logger.info("Socket.IO deshabilitado en este Web Service (SOCKETIO_ENABLED=false)");
            return;
        }

        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);

        server.addEventListener("room:create", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            String username = client.get("username");

            if (userId == null){
                client.sendEvent("room:error", Map.of("message", "No autenticado"));
                return;
            }

            String name = (String) data.getOrDefault("name", "Sala sin nombre");
            int capacity = (int) data.getOrDefault("capacity", 4);
            Room room = roomManager.createRoom(name, capacity, userId);

            server.getBroadcastOperations().sendEvent("room:created", RoomResponse.from(room));
        });

        server.addEventListener("room:list", Object.class, (client, data, ack) -> {
            List<RoomResponse> rooms = roomManager.listRooms().stream()
                    .map(RoomResponse::from)
                    .toList();
            client.sendEvent("room:list", rooms);
        });

        server.addEventListener("room:join", Map.class, (client, data, ack) -> {
            Long userId = client.get("userId");
            String username = client.get("username");
            String roomId = (String) data.get("roomId");

            if (userId == null || roomId == null){
                client.sendEvent("room:error", Map.of("message", "Datos invalidos"));
                return;
            }

            RoomManager.JoinResult result = roomManager.joinRoom(roomId, userId, username, client.getSessionId().toString());

            if (!result.success()){
                client.sendEvent("room:error", Map.of("message", result.error()));
                return;
            }

            client.joinRoom(roomId);
            client.set("roomId", roomId);
            RoomResponse roomResponse = RoomResponse.from(result.room());

            server.getRoomOperations(roomId).sendEvent("player:joined", Map.of(
                    "roomId", roomId,
                    "player", Map.of("userId", userId, "username", username),
                    "room", roomResponse
            ));

            if (result.gameStarted()) {
                server.getRoomOperations(roomId).sendEvent("game:start", Map.of(
                        "roomId", roomId,
                        "startedAt", System.currentTimeMillis()
                ));
                logger.info("game:start emitido roomId={}", roomId);
            }

        });
    }

    private void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");

        if (token == null || !jwtService.isValid(token)) {
            logger.warn("Conexión de socket rechazada: token inválido roomId={}", client.getSessionId());
            client.disconnect();
            return;
        }

        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        client.set("userId", userId);
        client.set("username", username);
        logger.info("Cliente conectado  userId={} sessionId={}", userId, client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        Long userId = client.get("userId");
        String roomId = client.get("roomId");
        logger.info("Cliente desconectado userId={} sessionId={}", userId, client.getSessionId());

        if (roomId != null){
            roomManager.removePlayer(client.getSessionId().toString());
            server.getRoomOperations(roomId).sendEvent("player:left", Map.of(
                    "userId", userId,
                    "roomId", roomId
            ));
        }
    }
}