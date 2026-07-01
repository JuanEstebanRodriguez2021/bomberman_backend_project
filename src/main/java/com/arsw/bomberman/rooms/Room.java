package com.arsw.bomberman.rooms;

import java.util.ArrayList;
import java.util.List;

public class Room{

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    private final String id;
    private final String name;
    private final int capacity;
    private final Long createdBy;
    private final List<Player> players = new ArrayList<>();
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
        return players;
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

    public void addPlayer(Player player){
        players.add(player);
    }

    public void removePlayer(String sessionId){
        players.removeIf(p -> p.sessionId().equals(sessionId));
    }

    public void setStatus(Status status){
        this.status = status;
    }
}
