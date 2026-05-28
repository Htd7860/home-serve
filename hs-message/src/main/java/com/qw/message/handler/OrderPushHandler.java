package com.qw.message.handler;

import com.qw.common.exception.BizException;
import com.qw.common.utils.JwtUtils;
import com.qw.message.constant.ErrorConstant;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    @Value("${ws.node.id}")
    private String nodeId;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long workId=getWorkerIdFromSession(session);
        sessions.put(workId,session);
        stringRedisTemplate.opsForValue().set("ws:node:worker:"+workId,nodeId,600, TimeUnit.SECONDS);
        log.info("服务者上线 work_id={} 当前在线人数={}",workId,sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long workId=getWorkerIdFromSession(session);
        sessions.remove(workId);
        stringRedisTemplate.delete("ws:node:worker:"+workId);
        log.info("服务者下线 work_id={}",workId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if("ping".equals(message.getPayload())){
            String key=getWorkerIdFromSession(session)+"";
            stringRedisTemplate.expire("ws:node:worker:"+key,10,TimeUnit.MINUTES);
        }
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
