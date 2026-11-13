package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Controller
public class SocketController {

    private final SocketService service;

    private final RoomService roomService;

    @MessageMapping("/join")
    public void handleJoin(String code) {
        Optional<Room> room = roomService.findByCode(code);
        service.broadcastAll(BroadcastEnum.UPDATE_ROOM, code, room.get());
    }
}
