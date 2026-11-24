package com.fyf.handler;

import com.fyf.result.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DataExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDuplicateKey(DataIntegrityViolationException e) {
        if (e.getMessage().contains("Duplicate entry")) {
            return Result.fail(500,"用户名已存在");
        }
        return Result.fail(500,"数据异常");
    }
}
