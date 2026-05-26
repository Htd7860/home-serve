package com.qw.message.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author：qw
 * @Package：com.qw.message.dto
 * @Project：home-serve
 * @name：OrderBroadcastMessage
 * @Date：2026/5/25 15:02
 * @Filename：OrderBroadcastMessage
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderBroadcastMessage {
    private Long orderId;
    private Integer categoryId;
    private BigDecimal lng;
    private BigDecimal lat;
    private BigDecimal finalPrice;
    private LocalDateTime appointmentTime;
}
