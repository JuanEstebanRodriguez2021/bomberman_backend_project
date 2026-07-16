package com.arsw.bomberman.rooms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Room{

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    private final String id;
    private final String name;
    private final int capacity;
    private final Long createdBy;
    private final Map<Long, Player> players = new LinkedHashMap<>();
    private Status status = Status.WAITING;

    public Room(String id, String name, int capacity, Long createdBy){
        this.id = id;
        this.name = name;
        this.capacity = Math.min(capacity, MAX_PLAYERS);
        this.createdBy = createdBy;
    }

    public record Player(Long userId, String username, String sessionId){}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public List<Player> getPlayers(){
        return new ArrayList<>(players.values());
    }

    public Status getStatus() {
        return status;
    }

    public boolean isFull(){
        return players.size() >= capacity;
    }

    public boolean canStart(){
        return players.size() >= MIN_PLAYERS && status == Status.WAITING;
    }

    public boolean isWaiting(){
        return status == Status.WAITING;
    }

    public boolean hasPlayer(Long userId){
        return players.containsKey(userId);
    }

    public void addPlayer(Player player){
        players.put(player.userId(), player);
    }

    public void updateSession(Long userId, String sessionId){
        Player existing = players.get(userId);
        if (existing != null){
            players.put(userId, new Player(userId, existing.username(), sessionId));
        }
    }

    public void removePlayer(String sessionId){
        players.values().removeIf(p -> p.sessionId().equals(sessionId));
    }

    public void setStatus(Status status){
        this.status = status;
    }
}
