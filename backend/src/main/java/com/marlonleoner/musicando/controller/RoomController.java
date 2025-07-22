package com.marlonleoner.musicando.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marlonleoner.musicando.aggregation.RoomAggregation;
import com.marlonleoner.musicando.domain.dto.RoomDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomAggregation aggregation;

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(aggregation.getAllRooms());
    }

    @PostMapping()
    public ResponseEntity<RoomDTO> createroom() {
        return ResponseEntity.ok(aggregation.createRoom());
    }
}
