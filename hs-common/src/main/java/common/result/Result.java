package common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：common.result
 * @Project：home-serve
 * @name：Result
 * @Date：2026/5/16 9:54
 * @Filename：Result
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result <T>{
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data){
       return new Result<T>(200,null,data);
    }

    public static Result ok(){
        return new Result(200,null,null);
    }

    public static<T> Result<T> fail(int code,String message){
        return new Result<T>(code,message,null);
    }

    public static<T> Result<T> fail(String message){
        return new Result<T>(401,message,null);
    }

}
