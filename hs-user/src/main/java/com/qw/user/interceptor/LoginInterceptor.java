package com.qw.user.interceptor;

import com.qw.user.annotation.RequireRole;
import common.utils.JwtUtils;
import common.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * @Author：qw
 * @Package：com.qw.user.interceptor
 * @Project：home-serve
 * @name：LoginInterceptor
 * @Date：2026/5/16 19:54
 * @Filename：LoginInterceptor
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader=request.getHeader("Authorization");
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return false;
        }
        String token=authHeader.substring(7);
        log.info("token:{}",token);
        try {
            Claims claims = JwtUtils.parseToken(token);
            UserContext.set(Integer.parseInt((String) claims.get("loginType")),Long.parseLong(claims.getSubject()));
        } catch (Exception e) {
            log.info("{}",e);
            response.setStatus(401);
            return false;
        }
        log.info("=====================================");
        if(handler instanceof HandlerMethod hm) {
            RequireRole methodAnnotation = hm.getMethodAnnotation(RequireRole.class);
            if (methodAnnotation != null) {
                String[] value = methodAnnotation.value();
                if (Arrays.asList(value).contains(UserContext.getLoginType())) {
                    return true;
                }
                response.setStatus(403);
                return false;
            }
        }
      return true;
    }
}
