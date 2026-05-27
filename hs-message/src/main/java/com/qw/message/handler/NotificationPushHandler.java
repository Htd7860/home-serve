package com.qw.message.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qw.common.entity.Notifications;
import com.qw.common.utils.JwtUtils;
import com.qw.message.constant.ErrorConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：qw
 * @Package：com.qw.message.handler
 * @Project：home-serve
 * @name：NotificationPushHandler
 * @Date：2026/5/27 14:34
 * @Filename：NotificationPushHandler
 */
@Slf4j
@Component
public class NotificationPushHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> sessions=new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String key=getKeyFromSession(session);
        sessions.put(key,session);
        log.info("用户上线:{}",key);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String key=getKeyFromSession(session);
        sessions.remove(key);
        log.info("用户下线:{}",key);
    }

    private String getKeyFromSession(WebSocketSession session){
        String token = session.getUri().getQuery().split("token=")[1];
        if(token==null){throw new IllegalStateException(ErrorConstant.WEBSOCKET_MISS_TOKEN);}
        Claims claims = JwtUtils.parseToken(token);
        String id = claims.getSubject();
        String type=(String) claims.get("loginType");
        Integer temp=Integer.valueOf(type)-1;
        return temp+":"+id;
    }

    public void send(Notifications notifications){
        String key=notifications.getReceiverType()+":"+notifications.getReceiverId();
        WebSocketSession webSocketSession = sessions.get(key);

        if(webSocketSession!=null&&webSocketSession.isOpen()){
            try {
                String json=new ObjectMapper().writeValueAsString(notifications);
                webSocketSession.sendMessage(new TextMessage(json));
            } catch (JsonProcessingException e) {
                log.error("{}",e);
                return;
            } catch (IOException e) {
                log.error("{}",e);
                sessions.remove(key);
                return;
            }
        }

    }
}
