package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 歌曲返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDTO {
    private Long id;

    /** 歌曲名称 */
    private String songName;

    /** 歌手 */
    private String singer;

    /** 音频地址 */
    private String url;

    private LocalDateTime createTime;
}
