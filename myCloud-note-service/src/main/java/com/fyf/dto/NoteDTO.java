package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {
    private Long id;

    /** 笔记内容 */
    private String note;

    /** 所属分组id */
    private Long typeId;

    private LocalDateTime createTime;
}
