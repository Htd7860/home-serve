package com.qw.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author：qw
 * @Package：com.qw.user.config
 * @Project：home-serve
 * @name：SwaggerConfig
 * @Date：2026/5/16 23:04
 * @Filename：SwaggerConfig
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 定义一个全局安全方案：Bearer Token 放在请求头
        SecurityScheme scheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 所有接口默认都要带这个头
        SecurityRequirement requirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .addSecurityItem(requirement)
                .schemaRequirement("bearerAuth", scheme);
    }
}
//eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIiwibG9naW5UeXBlIjoiMyIsImlhdCI6MTc3ODk0MzQzMSwiZXhwIjoxNzc4OTUwNjMxfQ.WJIQmjosAbTtkBLl-zqQbOPQ90qnQCi861Rw5fGhEO2rq4sX4RBlkjZft48IMy_Z