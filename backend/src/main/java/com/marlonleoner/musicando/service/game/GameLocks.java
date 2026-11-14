package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.exception.BaseException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Um lock por sala: as transições do timer e as respostas dos jogadores alteram o mesmo estado da partida. Considera
 * uma única instância do backend.
 */
@Component
public class GameLocks {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public void run(String roomCode, LockedAction action) throws BaseException {
        final ReentrantLock lock = locks.computeIfAbsent(roomCode, key -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public void execute(String roomCode, Runnable action) {
        final ReentrantLock lock = locks.computeIfAbsent(roomCode, key -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public void release(String roomCode) {
        locks.remove(roomCode);
    }

    @FunctionalInterface
    public interface LockedAction {

        void run() throws BaseException;
    }
}
