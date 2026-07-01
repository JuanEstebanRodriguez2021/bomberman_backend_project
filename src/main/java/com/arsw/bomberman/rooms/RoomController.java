package com.arsw.bomberman.rooms;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController{

    private final RoomManager roomManager;

    public RoomController(RoomManager roomManager){
        this.roomManager = roomManager;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> listRooms(){
        List<RoomResponse> rooms = roomManager.listRooms().stream()
                .map(RoomResponse::from)
                .toList();
        return ResponseEntity.ok(rooms);
    }
}