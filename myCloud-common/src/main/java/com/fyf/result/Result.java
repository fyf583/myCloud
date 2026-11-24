package com.fyf.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
