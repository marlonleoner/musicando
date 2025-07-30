package com.marlonleoner.musicando.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

    @MessageMapping("/join")
    @SendTo("/topic/room")
    public String handleJoin(String message) {
        // lógica para entrada do jogador, broadcast para todos da sala
        return "Player " + message + " joined!";
    }
}