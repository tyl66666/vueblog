package com.tyl.vueblog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T>  implements Serializable {
    private Integer code;
    private String msg;
    private  T data;


    public static <T> Result<T> success(){
        return new Result<T>(200,"success",null);
    }

    public static <T> Result<T> success(T data){
        return new Result<T>(200,"success",data);
    }

    public static <T> Result<T> success(T data,String message){
        return new Result<T>(200,message,data);
    }

    public static <T> Result<T> success(String message){
        return new Result<T>(200,message,null);
    }




    public static <T> Result<T> fail(){
        return new Result<T>(404,"fa  il",null);
    }

    public static <T> Result<T> fail(Integer code){
        return new Result<T>(code,"fail",null);
    }

    public static <T> Result<T> fail(Integer code,String message){
        return new Result<T>(code,message,null);
    }

    public static <T> Result<T> fail(String message){
        return new Result<T>(404,message,null);
    }
}
