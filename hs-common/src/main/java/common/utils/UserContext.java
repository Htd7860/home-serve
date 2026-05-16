package common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author：qw
 * @Package：common.utils
 * @Project：home-serve
 * @name：UserContext
 * @Date：2026/5/16 20:17
 * @Filename：UserContext
 */
public class UserContext {
public static ThreadLocal<Object[]>  threadLocal=new ThreadLocal<>();

public static void set(Integer LoginType,Long userId){
    threadLocal.set(new Object[]{LoginType,userId});
}

    public static Integer getLoginType(){
    return (Integer) threadLocal.get()[0];
    }

    public static Long getUserId(){
    return (Long)threadLocal.get()[1];
    }

    public static void clear(){
    threadLocal.remove();
    }

}
