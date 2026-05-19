package com.qw.user;

import com.qw.common.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

/**
 * @Author：qw
 * @Package：com.qw.user
 * @Project：home-serve
 * @name：MainTest
 * @Date：2026/5/16 13:15
 * @Filename：MainTest
 */
public class MainTest {
    @Test
    void test(){
        String abc12345 = PasswordUtils.encode("Abc12345");
        System.out.println(abc12345);

    }
}
