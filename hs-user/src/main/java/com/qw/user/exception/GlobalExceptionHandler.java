package com.qw.user.exception;

import com.qw.common.result.Result;
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
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException methodArgumentNotValidException){
        return Result.fail(methodArgumentNotValidException.getMessage());
    }

}
