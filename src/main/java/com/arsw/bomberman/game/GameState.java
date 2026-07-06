package com.arsw.bomberman.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private static final int[][] START_POSITIONS = {
        {1, 1}, {1, 11}, {9, 1}, {9, 11}
    };
    private final String roomId;
    private final GameMap map;
    private final Map<Long, Player> players = new ConcurrentHashMap<>();
    private final List<Bomb> activeBombs = Collections.synchronizedList(new ArrayList<>());
    private boolean finished = false;
    private Long winnerId = null;

    public GameState(String roomId, List<com.arsw.bomberman.rooms.Room.Player> roomPlayers) {
        this.roomId = roomId;
        this.map = new GameMap();
        int i = 0;
        for (com.arsw.bomberman.rooms.Room.Player rp : roomPlayers) {
            int[] pos = START_POSITIONS[i % START_POSITIONS.length];
            players.put(rp.userId(), new Player(rp.userId(), rp.username(), pos[1], pos[0]));
            i++;
        }
    }

    public record Bomb(Long userId, int x, int y, long placedAt) {}

    public String getRoomId() { 
        return roomId; 
    }

    public GameMap getMap() { 
        return map; 
    }

    public Map<Long, Player> getPlayers() { 
        return players; 
    }

    public List<Bomb> getActiveBombs() { 
        return activeBombs; 
    }

    public boolean isFinished() { 
        return finished; 
    }

    public Long getWinnerId() { 
        return winnerId; 
    }

    public Optional<Player> getPlayer(Long userId) {
        return Optional.ofNullable(players.get(userId));
    }

    public void setFinished(Long winnerId) {
        this.finished = true;
        this.winnerId = winnerId;
    }

    public List<Player> getAlivePlayers() {
        return players.values().stream().filter(Player::isAlive).toList();
    }
}