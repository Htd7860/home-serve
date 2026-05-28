package com.qw.user.exception;

import com.qw.common.exception.BizException;
import com.qw.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author：qw
 * @Package：common.exception
 * @Project：home-serve
 * @name：GlobalExceptionHandler
 * @Date：2026/5/16 14:09
 * @Filename：GlobalExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException methodArgumentNotValidException){
        return Result.fail(methodArgumentNotValidException.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException){
        return Result.fail(bizException.getCode(),bizException.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatch() {
        return Result.fail("参数类型错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleNotReadable() {
        return Result.fail("请求格式错误");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未捕获异常", e);
        return Result.fail("系统繁忙,请稍后重试");
    }
}
