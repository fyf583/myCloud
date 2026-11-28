package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 相册照片实体（对应表 album）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Album {
    private Long id;

    private Long userId;

    /** 照片名称 */
    private String photoName;

    /** 照片描述 */
    private String description;

    /** 照片访问地址（相对路径，如 /album/file/xxx.jpg） */
    private String url;

    /** 1 正常、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
