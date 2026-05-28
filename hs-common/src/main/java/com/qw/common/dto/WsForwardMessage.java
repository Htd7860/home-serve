package com.qw.common.dto;

import com.qw.common.entity.Notifications;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.common.dto
 * @Project：home-serve
 * @name：WsForwardMessage
 * @Date：2026/5/27 23:49
 * @Filename：WsForwardMessage
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsForwardMessage {
    private Integer type;              // 1=通知  2=订单广播
    private Notifications notification; // type=1 有值
    private Long workerId;             // type=2 有值
    private String payload;            // type=2 订单 JSON
}