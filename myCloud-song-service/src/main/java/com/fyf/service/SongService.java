package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.SongDTO;
import com.fyf.result.CursorPageResult;

public interface SongService {

    /** 歌单列表（游标分页） */
    CursorPageResult<SongDTO> songList(CursorPage cursorPage);

    /** 添加歌曲 */
    void addSong(SongDTO songDTO);

    /** 播放歌曲：返回歌曲信息（含音频地址） */
    SongDTO getSong(Long songId);

    /** 删除歌曲（软删除） */
    void deleteSong(Long songId);
}
