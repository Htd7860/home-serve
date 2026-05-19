package com.qw.common.utils;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author：qw
 * @Package：common.utils
 * @Project：home-serve
 * @name：RandomTTL
 * @Date：2026/5/17 21:56
 * @Filename：RandomTTL
 */
public class RandomTTL {
   public static Duration randomTTL(long minutes){
       double random= ThreadLocalRandom.current().nextDouble()*0.8;
       return Duration.ofMillis((long) (minutes*1000*60*minutes*random));
   }
}
