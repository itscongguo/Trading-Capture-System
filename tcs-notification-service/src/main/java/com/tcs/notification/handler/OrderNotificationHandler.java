package com.tcs.notification.handler;

import com.tcs.notification.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket handler for order notifications
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserIdFromPath(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);
            log.info("WebSocket connection established for user: {}", userId);
        } else {
            log.warn("WebSocket connection without valid userId");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if needed (e.g., heartbeat)
        log.debug("Received message from client: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserIdFromPath(session);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
            log.info("WebSocket connection closed for user: {}, status: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = extractUserIdFromPath(session);
        log.error("WebSocket transport error for user: {}", userId, exception);
        sessionManager.removeSession(userId, session);
    }

    private String extractUserIdFromPath(WebSocketSession session) {
        String path = session.getUri().getPath();
        // Extract userId from path like /ws/orders/{userId}
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}
