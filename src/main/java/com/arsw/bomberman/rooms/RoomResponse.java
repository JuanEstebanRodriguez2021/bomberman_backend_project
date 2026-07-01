package com.arsw.bomberman.rooms;

import java.util.List;

public record RoomResponse(String id, String name, int capacity, int playerCount, String status, List<PlayerInfo> players){

    public record PlayerInfo(Long userId, String username){ }

    public static RoomResponse from(Room room){
        List<PlayerInfo> players = room.getPlayers().stream()
                .map(p -> new PlayerInfo(p.userId(), p.username()))
                .toList();
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getPlayers().size(),
                room.getStatus().name().toLowerCase(),
                players
        );
    }
}