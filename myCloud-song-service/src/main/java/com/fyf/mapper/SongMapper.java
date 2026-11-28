package com.fyf.mapper;

import com.fyf.dto.SongDTO;
import com.fyf.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SongMapper {

    /** 游标分页查询歌曲 */
    List<SongDTO> getSongs(@Param("pageSize") Integer pageSize,
                           @Param("cursorId") Long cursorId,
                           @Param("cursorTime") LocalDateTime cursorTime,
                           @Param("userId") Long userId,
                           @Param("status") Integer status);

    /** 查询单首歌曲（带 userId，防止越权） */
    SongDTO getSongById(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("status") Integer status);

    /** 添加歌曲 */
    Integer insertSong(Song song);

    /** 修改 / 软删除歌曲（带 userId，防止越权） */
    Integer updateSong(@Param("song") Song song, @Param("userId") Long userId);
}
