package com.arsw.bomberman.game;

import com.arsw.bomberman.events.*;
import com.arsw.bomberman.rooms.Room;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class GameEngine {

    private static final int BOMB_TIMER_SECONDS = 3;
    private static final int EXPLOSION_RADIUS = 2;

    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public GameEngine(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    public GameState initGame(String roomId, List<Room.Player> players) {
        GameState state = new GameState(roomId, players);
        activeGames.put(roomId, state);
        return state;
    }

    public Optional<GameState> getState(String roomId) {
        return Optional.ofNullable(activeGames.get(roomId));
    }

    public MoveResult movePlayer(String roomId, Long userId, String direction, long clientTimestamp) {
        GameState state = activeGames.get(roomId);
        if (state == null || state.isFinished()) return MoveResult.invalid("Partida no activa");

        Player player = state.getPlayers().get(userId);
        if (player == null || !player.isAlive()) return MoveResult.invalid("Jugador no encontrado");

        int newX = player.getX();
        int newY = player.getY();

        switch (direction.toLowerCase()) {
            case "up"    -> newY--;
            case "down"  -> newY++;
            case "left"  -> newX--;
            case "right" -> newX++;
            default -> { return MoveResult.invalid("Dirección inválida"); }
        }

        if (!state.getMap().isWalkable(newY, newX)) {
            return MoveResult.invalid("Movimiento bloqueado");
        }

        boolean bombInTarget = state.getActiveBombs().stream()
            .anyMatch(b -> b.x() == newX && b.y() == newY);
        if (bombInTarget) return MoveResult.invalid("Celda bloqueada por bomba");

        player.setPosition(newX, newY);

        long latencyMs = System.currentTimeMillis() - clientTimestamp;
        eventPublisher.publishEvent(
            new PlayerMovedEvent(roomId, userId, newX, newY, latencyMs)
        );

        return MoveResult.ok(newX, newY);
    }

    
    public BombResult placeBomb(String roomId, Long userId) {
        GameState state = activeGames.get(roomId);
        if (state == null || state.isFinished()) return BombResult.invalid("Partida no activa");

        Player player = state.getPlayers().get(userId);
        if (player == null || !player.isAlive()) return BombResult.invalid("Jugador no válido");
        if (player.getBombsAvailable() <= 0) return BombResult.invalid("Sin bombas disponibles");

        int bx = player.getX();
        int by = player.getY();

        boolean alreadyBomb = state.getActiveBombs().stream().anyMatch(b -> b.x() == bx && b.y() == by);
        if (alreadyBomb) return BombResult.invalid("Ya hay una bomba ahí");

        GameState.Bomb bomb = new GameState.Bomb(userId, bx, by, System.currentTimeMillis());
        state.getActiveBombs().add(bomb);
        player.useBomb();

        eventPublisher.publishEvent(new BombPlacedEvent(roomId, userId, bx, by));

        scheduler.schedule(() -> explodeBomb(roomId, bomb), BOMB_TIMER_SECONDS, TimeUnit.SECONDS);

        return BombResult.ok(bx, by);
    }

    private void explodeBomb(String roomId, GameState.Bomb bomb) {
        GameState state = activeGames.get(roomId);
        if (state == null) return;

        state.getActiveBombs().remove(bomb);

        Player bomber = state.getPlayers().get(bomb.userId());
        if (bomber != null) bomber.returnBomb();

        List<int[]> affectedCells = new ArrayList<>();
        List<Long> eliminated = new ArrayList<>();

        int[][] directions = {{0,1},{0,-1},{1,0},{-1,0}};
        affectedCells.add(new int[]{bomb.x(), bomb.y()});

        for (int[] dir : directions) {
            for (int i = 1; i <= EXPLOSION_RADIUS; i++) {
                int cx = bomb.x() + dir[0] * i;
                int cy = bomb.y() + dir[1] * i;

                int cell = state.getMap().getCell(cy, cx);
                if (cell == GameMap.WALL) break; 

                affectedCells.add(new int[]{cx, cy});

                if (cell == GameMap.BLOCK) {
                    state.getMap().destroyBlock(cy, cx); 
                    break; 
                }

                state.getPlayers().values().stream()
                    .filter(p -> p.isAlive() && p.getX() == cx && p.getY() == cy)
                    .forEach(p -> {
                        p.eliminate();
                        eliminated.add(p.getUserId());
                        eventPublisher.publishEvent(
                            new PlayerEliminatedEvent(roomId, p.getUserId())
                        );
                    });
            }
        }

        eventPublisher.publishEvent(
            new BombExplodedEvent(roomId, bomb.x(), bomb.y(), affectedCells, eliminated)
        );

        checkGameOver(state);
    }

    private void checkGameOver(GameState state) {
        List<Player> alive = state.getAlivePlayers();
        if (alive.size() <= 1) {
            Long winnerId = alive.isEmpty() ? null : alive.get(0).getUserId();
            String winnerUsername = alive.isEmpty() ? null : alive.get(0).getUsername();
            state.setFinished(winnerId);
            eventPublisher.publishEvent(
                new GameFinishedEvent(state.getRoomId(), winnerId, winnerUsername)
            );
        }
    }

    public void removeGame(String roomId) {
        activeGames.remove(roomId);
    }

    public record MoveResult(boolean valid, int x, int y, String error) {
        static MoveResult ok(int x, int y) { return new MoveResult(true, x, y, null); }
        static MoveResult invalid(String error) { return new MoveResult(false, 0, 0, error); }
    }

    public record BombResult(boolean valid, int x, int y, String error) {
        static BombResult ok(int x, int y) { return new BombResult(true, x, y, null); }
        static BombResult invalid(String error) { return new BombResult(false, 0, 0, error); }
    }
}