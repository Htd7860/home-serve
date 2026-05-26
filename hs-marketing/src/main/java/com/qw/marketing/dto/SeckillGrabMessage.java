package com.qw.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.marketing.dto
 * @Project：home-serve
 * @name：SeckillGrabMessage
 * @Date：2026/5/26 16:13
 * @Filename：SeckillGrabMessage
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGrabMessage {
    Long userId;
    Long activityId;
    Long templateId;
}
