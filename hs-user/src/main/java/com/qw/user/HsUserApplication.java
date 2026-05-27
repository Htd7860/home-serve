package com.qw.user;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author qw
 * 启动类
 */
@SpringBootApplication(scanBasePackages = "com.qw")
@MapperScan("com.qw")
@EnableScheduling
public class HsUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(HsUserApplication.class,args);
    }
}
