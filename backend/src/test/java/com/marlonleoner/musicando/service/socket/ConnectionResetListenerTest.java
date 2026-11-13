package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConnectionResetListenerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private ConnectionResetListener listener;

    @Test
    void shouldResetRoomAndPlayerConnectionsWhenApplicationStarts() {
        listener.resetConnections();

        verify(roomService).resetConnections();
        verify(playerService).resetConnections();
    }
}
