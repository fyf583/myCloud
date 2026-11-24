package com.fyf.handler;

import com.fyf.exception.BaseException;
import com.fyf.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result handlerBaseException(BaseException e) {
        return Result.fail(500, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handlerException(Exception e) {
        return Result.fail(500, e.getMessage());
    }
}
