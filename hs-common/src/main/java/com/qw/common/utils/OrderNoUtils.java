package com.qw.common.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.data.redis.connection.stream.StreamInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author：qw
 * @Package：com.qw.common.utils
 * @Project：home-serve
 * @name：OrderNoUtils
 * @Date：2026/5/19 18:37
 * @Filename：OrderNoUtils
 */
public class OrderNoUtils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER=DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateOrderNo(){
        return LocalDateTime.now().format(DATE_TIME_FORMATTER)+ IdWorker.getId();
    }

}
