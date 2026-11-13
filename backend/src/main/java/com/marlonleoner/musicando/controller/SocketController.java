package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class SocketController {

    private final SocketService service;

    private final RoomService roomService;

    private final RoomMapper roomMapper;

    @MessageMapping("/join")
    public void handleJoin(String code) {
        roomService.findByCode(code)
                .map(roomMapper::toStateBroadcast)
                .ifPresent(state -> service.broadcastAll(BroadcastEnum.UPDATE_ROOM, code, state));
    }
}
