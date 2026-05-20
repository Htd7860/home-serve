package com.qw.common.exception;

/**
 * @Author：qw
 * @Package：com.qw.common.exception
 * @Project：home-serve
 * @name：BizException
 * @Date：2026/5/19 17:45
 * @Filename：BizException
 */

import lombok.Getter;

@Getter
public class BizException extends RuntimeException{
    private final int code;
    public BizException(int code,String msg){
        super(msg);
        this.code=code;
    }

    public BizException(String msg){
        this(400,msg);
    }
}
