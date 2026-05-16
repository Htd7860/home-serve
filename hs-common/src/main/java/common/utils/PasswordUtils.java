package common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Author：qw
 * @Package：common.utils
 * @Project：home-serve
 * @name：PasswordUtils
 * @Date：2026/5/16 15:01
 * @Filename：PasswordUtils
 */
public class PasswordUtils {
    public static final BCryptPasswordEncoder ENCODER=new BCryptPasswordEncoder();

    public static String encode(String rawPassword){
    return ENCODER.encode(rawPassword);
    }

    public static boolean match(String rawPassword,String encodePassword){
        return ENCODER.matches(rawPassword,encodePassword);
    }
}
