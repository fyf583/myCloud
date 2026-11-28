package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件实体（对应表 file）
 * <p>
 * 注意：类名不叫 File，避免与 java.io.File 冲突
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
    private Long id;

    private Long userId;

    /** 展示用文件名（原始文件名或用户自定义） */
    private String fileName;

    /** 文件描述 */
    private String description;

    /** 文件大小（字节） */
    private Long size;

    /** 磁盘上的文件名（UUID.ext） */
    private String storedName;

    /** 1 正常、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
