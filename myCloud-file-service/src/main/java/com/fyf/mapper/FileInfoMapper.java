package com.fyf.mapper;

import com.fyf.dto.FileDTO;
import com.fyf.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileInfoMapper {

    /** 游标分页查询文件 */
    List<FileDTO> getFiles(@Param("pageSize") Integer pageSize,
                           @Param("cursorId") Long cursorId,
                           @Param("cursorTime") LocalDateTime cursorTime,
                           @Param("userId") Long userId,
                           @Param("status") Integer status);

    /** 按 id 查询（带 userId，防止越权） */
    FileDTO getFileById(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("status") Integer status);

    /** 按磁盘文件名查询（下载接口用，无登录态） */
    FileDTO getFileByStoredName(@Param("storedName") String storedName,
                                @Param("status") Integer status);

    /** 新增文件记录 */
    Integer insertFile(FileInfo file);

    /** 修改 / 软删除（带 userId，防止越权） */
    Integer updateFile(@Param("file") FileInfo file, @Param("userId") Long userId);
}
