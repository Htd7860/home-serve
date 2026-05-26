package com.qw.message.handler;

import com.qw.common.exception.BizException;
import com.qw.common.utils.JwtUtils;
import com.qw.message.constant.ErrorConstant;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：qw
 * @Package：com.qw.message.handler
 * @Project：home-serve
 * @name：OrderPushHandler
 * @Date：2026/5/21 11:28
 * @Filename：OrderPushHandler
 */
@Slf4j
@Component
public class OrderPushHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<Long, WebSocketSession> sessions=new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long workId=getWorkerIdFromSession(session);
        sessions.put(workId,session);
        log.info("服务者上线 work_id={} 当前在线人数={}",workId,sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long workId=getWorkerIdFromSession(session);
        sessions.remove(workId);
        log.info("服务者下线 work_id={}",workId);
    }

    public void pushToWorker(Long workerId,String message)  {
        WebSocketSession session = sessions.get(workerId);
        if(session!=null&&session.isOpen()){
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("推送失败 worker_id{}",workerId);
                sessions.remove(workerId);
            }
        }
    }

    public void broadcastToNearby(List<Long> workerIds,String message){
        for (Long workerId : workerIds) {
            pushToWorker(workerId,message);
        }
    }

    private Long  getWorkerIdFromSession(WebSocketSession webSocketSession){
        String token = webSocketSession.getUri().getQuery().split("token=")[1];
       if(token==null){throw new IllegalStateException(ErrorConstant.WEBSOCKET_MISS_TOKEN);
       }
        Claims claims = JwtUtils.parseToken(token);
        return Long.valueOf(claims.getSubject());
    }
}
