package com.qw.common.utils;

/**
 * @Author：qw
 * @Package：common.utils
 * @Project：home-serve
 * @name：RandomAuthCode
 * @Date：2026/5/16 12:43
 * @Filename：RandomAuthCode
 */
public class RandomAuthCode {
    public static char[] arr=new char[62];
    static{
        int index=0;
        for(int i=0;i<10;i++,index++){
            arr[index]=(char)(i+'0');
        }
        for(int i=0;i<26;i++,index++){
            arr[index]=(char)(i+'a');
        }
        for(int i=0;i<26;i++,index++){
            arr[index]=(char)(i+'A');
        }

    }
    public static String get(int  len){
        StringBuilder res=new StringBuilder();
        while(len>0){
            int num=(int)(Math.random()*1000);num=num%62;
            res.append(arr[num]);len--;
        }
        return res.toString();
    }
}
