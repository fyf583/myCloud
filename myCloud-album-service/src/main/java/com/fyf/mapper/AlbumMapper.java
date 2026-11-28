package com.fyf.mapper;

import com.fyf.dto.AlbumDTO;
import com.fyf.entity.Album;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AlbumMapper {

    /** 游标分页查询照片 */
    List<AlbumDTO> getAlbums(@Param("pageSize") Integer pageSize,
                             @Param("cursorId") Long cursorId,
                             @Param("cursorTime") LocalDateTime cursorTime,
                             @Param("userId") Long userId,
                             @Param("status") Integer status);

    /** 查询单张照片（带 userId，防止越权查看他人照片） */
    AlbumDTO getAlbumById(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("status") Integer status);

    /** 新增照片记录 */
    Integer insertAlbum(Album album);

    /** 修改 / 软删除照片（带 userId，防止越权修改） */
    Integer updateAlbum(@Param("album") Album album, @Param("userId") Long userId);
}
