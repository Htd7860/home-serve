package com.qw.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Notifications {
    private Long id;
    private Integer receiverType;
    private Long receiverId;
    private String title;
    private String content;
    private Integer notificationType;
    private Long relatedOrderId;
    private Integer isRead;
    private LocalDateTime createdAt;
}