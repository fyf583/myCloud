package com.fyf.handler;

import com.fyf.exception.BaseException;
import com.fyf.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result handlerBaseException(BaseException e) {
        return Result.fail(500, e.getMessage());
    }

    /**
     * 静态资源（如相册图片 /album/file/xxx.jpg）不存在时返回真正的 404。
     * 否则会被下面的兜底 handler 包成 HTTP 200 + code 500，浏览器 &lt;img&gt; 无法正确判断加载失败。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handlerNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(404, "资源不存在：" + e.getResourcePath()));
    }

    /** 上传文件超过 spring.servlet.multipart.max-file-size 限制时的友好提示 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handlerMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(500, "上传文件过大，请压缩后重试");
    }

    @ExceptionHandler(Exception.class)
    public Result handlerException(Exception e) {
        return Result.fail(500, e.getMessage());
    }
}
