package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Integer id;

    private Integer userId;

    private String message;

    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
