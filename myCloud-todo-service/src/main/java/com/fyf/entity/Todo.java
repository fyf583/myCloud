package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 待做日程实体（对应表 todo）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    private Long id;

    private Long userId;

    /** 待做内容 */
    private String content;

    /** 截止时间，可为空 */
    private LocalDateTime deadline;

    /** 0 未完成、1 已完成、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
