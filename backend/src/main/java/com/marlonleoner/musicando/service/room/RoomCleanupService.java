package com.marlonleoner.musicando.service.room;

import com.marlonleoner.musicando.config.properties.CleanupProperties;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * Limpeza periódica das salas: encerra as abandonadas (sem a TV há {@code musicando.cleanup.abandoned-after}) e apaga
 * as encerradas há mais de {@code musicando.cleanup.closed-retention}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomCleanupService {

    private final RoomService roomService;

    private final GameEngine gameEngine;

    private final RoomStateNotifier notifier;

    private final CleanupProperties properties;

    private final Clock clock;

    @Scheduled(fixedDelayString = "${musicando.cleanup.interval:5m}", initialDelayString = "${musicando.cleanup.interval:5m}")
    public void cleanUp() {
        final int closed = closeAbandonedRooms();
        final int deleted = deleteClosedRooms();

        if (closed > 0 || deleted > 0) {
            log.info("Room cleanup: {} abandoned room(s) closed and {} closed room(s) deleted", closed, deleted);
        }
    }

    public int closeAbandonedRooms() {
        final List<Room> rooms = roomService.findAbandoned(before(properties.abandonedAfter()));
        int closed = 0;

        for (Room room : rooms) {
            try {
                gameEngine.stop(room.getCode());
                roomService.close(room);
                notifier.broadcast(room.getCode(), RoomStateReason.ROOM_CLOSED, null);
                closed++;
            } catch (DataAccessException | ObjectNotFoundException ex) {
                log.warn("Could not close abandoned room {}: {}", LogSanitizer.sanitize(room.getCode()), LogSanitizer.sanitize(ex.getMessage()));
            }
        }

        return closed;
    }

    public int deleteClosedRooms() {
        final List<Room> rooms = roomService.findClosedBefore(before(properties.closedRetention()));
        int deleted = 0;

        for (Room room : rooms) {
            try {
                roomService.delete(room);
                deleted++;
            } catch (DataAccessException ex) {
                log.warn("Could not delete closed room {}: {}", LogSanitizer.sanitize(room.getCode()), LogSanitizer.sanitize(ex.getMessage()));
            }
        }

        return deleted;
    }

    private Date before(Duration duration) {
        return Date.from(clock.instant().minus(duration));
    }
}
