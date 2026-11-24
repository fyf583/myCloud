package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Integer id;

    private String username;

    private String password;

    private Integer status;

    private String phone;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
