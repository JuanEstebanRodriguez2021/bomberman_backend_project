package com.arsw.bomberman.rooms;

import com.arsw.bomberman.events.GameStartedEvent;
import com.arsw.bomberman.events.RoomCreatedEvent;
import com.arsw.bomberman.events.RoomErrorEvent;
import com.arsw.bomberman.events.RoomJoinedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RoomManager {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public RoomManager(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    public Room createRoom(String name, int capacitiy, Long createdBy){
        String roomId = "room_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        Room room = new Room(roomId, name, capacitiy, createdBy);
        rooms.put(roomId, room);
        eventPublisher.publishEvent(new RoomCreatedEvent(roomId, name, capacitiy, createdBy));
        return room;
    }

    public JoinResult joinRoom(String roomId, Long userId, String username, String sessionId) {
        Room room = rooms.get(roomId);

        if (room == null){
            eventPublisher.publishEvent(new RoomErrorEvent(roomId, userId, "room_not_found"));
            return JoinResult.error("La sala no existe");
        }

        if (room.isFull()){
            eventPublisher.publishEvent(new RoomErrorEvent(roomId, userId, "room_full"));
            return JoinResult.error("La sala está llena");
        }

        if (!room.isWaiting()){
            eventPublisher.publishEvent(new RoomErrorEvent(roomId, userId, "game_already_started"));
            return JoinResult.error("La partida ya comenzó");
        }

        room.addPlayer(new Room.Player(userId, username, sessionId));
        eventPublisher.publishEvent(new RoomJoinedEvent(roomId, userId, username, room.getPlayers().size()));

        if (room.canStart()){
            room.setStatus(Status.PLAYING);
            List<Long> playerIds = room.getPlayers().stream().map(Room.Player::userId).toList();
            eventPublisher.publishEvent(new GameStartedEvent(roomId, playerIds));
            return JoinResult.success(room, true);
        }

        return JoinResult.success(room, true);
    }

    public void removePlayer(String sessionId){
        rooms.values().forEach(room ->{
            int before = room.getPlayers().size();
            room.removePlayer(sessionId);
            if (before > 0 && room.getPlayers().isEmpty()){
                rooms.remove(room.getId());
            }
        });
    }

    public List<Room> listRooms(){
        return new ArrayList<>(rooms.values());
    }

    public Optional<Room> getRoom(String roomId){
        return Optional.ofNullable(rooms.get(roomId));
    }

    public record JoinResult(boolean success, Room room, boolean gameStarted, String error){
        static JoinResult success(Room room, boolean gameStarted){
            return new JoinResult(true, room, gameStarted, null);
        }
        static JoinResult error(String message){
            return new JoinResult(false, null, false, message);
        }
    }

}