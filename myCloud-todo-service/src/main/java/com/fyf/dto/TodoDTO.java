package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 日程返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDTO {
    private Long id;

    /** 待做内容 */
    private String content;

    /** 截止时间，可为空 */
    private LocalDateTime deadline;

    /** 0 未完成、1 已完成 */
    private Integer status;

    private LocalDateTime createTime;
}
