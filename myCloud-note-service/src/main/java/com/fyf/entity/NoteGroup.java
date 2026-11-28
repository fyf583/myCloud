package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记分组实体（对应表 note_group）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteGroup {
    private Long id;

    private Long userId;

    /** 分组名称 */
    private String groupName;

    /** 1 正常、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
