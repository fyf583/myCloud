package com.fyf.entity;

import com.fyf.annotation.AutoFill;
import com.fyf.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 歌曲实体（对应表 song）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Song {
    private Long id;

    private Long userId;

    /** 歌曲名称 */
    private String songName;

    /** 歌手 */
    private String singer;

    /** 音频地址（可公开访问的 mp3 / m4a 等） */
    private String url;

    /** 1 正常、2 已删除 */
    private Integer status;

    @AutoFill(value = OperateType.INSERT)
    private LocalDateTime createTime;

    @AutoFill(value = OperateType.UPDATE)
    private LocalDateTime updateTime;
}
