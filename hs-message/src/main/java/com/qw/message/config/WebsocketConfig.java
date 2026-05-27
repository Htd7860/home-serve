package com.qw.message.config;

import com.qw.common.entity.Notifications;
import com.qw.message.handler.NotificationPushHandler;
import com.qw.message.handler.OrderPushHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @Author：qw
 * @Package：com.qw.message.cofig
 * @Project：home-serve
 * @name：WebsocketConfig
 * @Date：2026/5/21 11:23
 * @Filename：WebsocketConfig
 */
@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer{

    @Autowired
    OrderPushHandler orderPushHandler;
    @Autowired
    NotificationPushHandler notificationPushHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderPushHandler,"/worker/orders/push").setAllowedOrigins("*");
        registry.addHandler(notificationPushHandler,"/notifications/push").setAllowedOrigins("*");
    }
}
