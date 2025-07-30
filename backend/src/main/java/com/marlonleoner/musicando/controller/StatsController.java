package com.marlonleoner.musicando.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import com.marlonleoner.musicando.domain.SocketStats;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final WebSocketMessageBrokerStats stats;

    @GetMapping
    public ResponseEntity<SocketStats> getAllStats() {
        return ResponseEntity.ok(new SocketStats(stats));
    }
}
