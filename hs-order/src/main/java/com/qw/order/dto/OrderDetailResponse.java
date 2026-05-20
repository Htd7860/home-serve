package com.qw.order.dto;

import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.order.dto
 * @Project：home-serve
 * @name：OrdeDetailResponse
 * @Date：2026/5/20 11:43
 * @Filename：OrdeDetailResponse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    Orders orders;
   List<OrderEvents> orderEvents;
}
