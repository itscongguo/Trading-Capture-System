package com.tcs.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.notification.dto.OrderStatusNotification;
import com.tcs.notification.dto.TradeNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Notification service for sending WebSocket messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * Send order status notification to user
     */
    public void sendOrderStatusNotification(OrderStatusNotification notification) {
        try {
            String message = objectMapper.writeValueAsString(notification);
            sessionManager.sendToUser(notification.getUserId(), message);
            log.info("Order status notification sent to user {}: orderId={}, status={}",
                    notification.getUserId(), notification.getOrderId(), notification.getStatus());
        } catch (JsonProcessingException e) {
            log.error("Error serializing order status notification: {}", e.getMessage());
        }
    }

    /**
     * Send trade notification to user
     */
    public void sendTradeNotification(TradeNotification notification) {
        try {
            String message = objectMapper.writeValueAsString(notification);
            sessionManager.sendToUser(notification.getUserId(), message);
            log.info("Trade notification sent to user {}: tradeId={}, orderId={}",
                    notification.getUserId(), notification.getTradeId(), notification.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing trade notification: {}", e.getMessage());
        }
    }
}
