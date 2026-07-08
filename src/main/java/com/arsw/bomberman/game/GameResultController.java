package com.arsw.bomberman.game;

import com.arsw.bomberman.auth.JwtAuthFilter;
import com.arsw.bomberman.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameResultController {

    private final GameResultRepository repository;
    private final JwtService jwtService;

    public GameResultController(GameResultRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    @GetMapping("/profile/games")
    public ResponseEntity<?> myGames(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }

        List<Map<String, Object>> games = repository
                .findByWinnerIdOrderByFinishedAtDesc(userId)
                .stream()
                .map(g -> Map.<String, Object>of(
                        "id", g.getId(),
                        "roomId", g.getRoomId(),
                        "result", "Victoria",
                        "finishedAt", g.getFinishedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(games);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        long todayGames = repository.countSince(
                Instant.now().truncatedTo(ChronoUnit.DAYS)
        );
        long totalGames = repository.count();

        return ResponseEntity.ok(Map.of(
                "partidasHoy", todayGames,
                "partidasTotales", totalGames,
                "timestamp", Instant.now().toString()
        ));
    }
}