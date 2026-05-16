package com.qw.user.config;

import com.qw.user.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author：qw
 * @Package：com.qw.user.config
 * @Project：home-serve
 * @name：WebConfig
 * @Date：2026/5/16 20:16
 * @Filename：WebConfig
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new LoginInterceptor())
               .addPathPatterns("/**")
               .excludePathPatterns(   "/auth/**",
                       "/swagger-ui/**",
                       "/swagger-resources/**",
                       "/v3/api-docs/**",
                       "/swagger-config/**");
    }
}
