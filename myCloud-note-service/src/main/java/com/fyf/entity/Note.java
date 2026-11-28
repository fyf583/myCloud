package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记实体（对应表 note）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    private Long id;

    private Long userId;

    /** 笔记内容 */
    private String note;

    /** 所属分组id，null 表示未分组 */
    private Long typeId;

    /** 1 正常、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
