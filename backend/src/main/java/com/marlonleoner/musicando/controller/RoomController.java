package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.aggregation.RoomAggregation;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.dto.request.JoinRoomRequest;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomAggregation aggregation;

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(aggregation.getAllRooms());
    }

    @PostMapping
    public ResponseEntity<RoomCreatedResponse> createRoom() {
        return ResponseEntity.ok(aggregation.createRoom());
    }

    @PostMapping("/join")
    public ResponseEntity<JoinRoomResponse> joinRoom(@RequestBody JoinRoomRequest params) throws BaseException {
        return ResponseEntity.ok(aggregation.joinRoom(params));
    }
}
