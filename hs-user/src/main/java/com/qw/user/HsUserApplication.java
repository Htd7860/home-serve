package com.qw.user;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author qw
 * 启动类
 */
@SpringBootApplication(scanBasePackages = "com.qw")
@MapperScan("com.qw")
public class HsUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(HsUserApplication.class,args);
    }
}
