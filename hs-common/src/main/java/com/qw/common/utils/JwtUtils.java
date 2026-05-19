package com.qw.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * @Author：qw
 * @Package：common.utils
 * @Project：home-serve
 * @name：Jwtutils
 * @Date：2026/5/16 17:45
 * @Filename：Jwtutils
 */
public class JwtUtils {
    private static final String SECRET = "home-serve-jwt-secret-key-must-be-256-bits-long!!";
    private static final SecretKey KEY= Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String createToken(Long userId, String loginType, long expireMillis, Map<String, Object> extra) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("loginType", loginType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .addClaims(extra)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public static boolean isExpired(String token){
        try {
            Claims claims = JwtUtils.parseToken(token);
            return false;
        }catch (Exception e){
            return true;
        }
    }

}
