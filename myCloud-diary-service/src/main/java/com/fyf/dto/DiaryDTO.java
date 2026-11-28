package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 日记返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryDTO {
    private Long id;

    /** 日记内容 */
    private String diary;

    private LocalDateTime createTime;
}
