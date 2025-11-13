package com.tcs.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manager for WebSocket sessions
 */
@Slf4j
@Service
public class WebSocketSessionManager {

    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * Add a WebSocket session for a user
     */
    public void addSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("Session added for user: {}, total sessions: {}", userId, userSessions.get(userId).size());
    }

    /**
     * Remove a WebSocket session for a user
     */
    public void removeSession(String userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
            log.info("Session removed for user: {}, remaining sessions: {}",
                    userId, sessions.size());
        }
    }

    /**
     * Send message to all sessions of a user
     */
    public void sendToUser(String userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("No active WebSocket sessions for user: {}", userId);
            return;
        }

        sessions.removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    return false;
                } else {
                    log.warn("Session is closed for user: {}", userId);
                    return true;
                }
            } catch (IOException e) {
                log.error("Error sending message to user: {}", userId, e);
                return true;
            }
        });

        log.debug("Message sent to {} sessions for user: {}", sessions.size(), userId);
    }

    /**
     * Get active session count for a user
     */
    public int getSessionCount(String userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Get total active sessions
     */
    public int getTotalSessionCount() {
        return userSessions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
}
