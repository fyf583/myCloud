package com.fyf.mapper;

import com.fyf.dto.DiaryDTO;
import com.fyf.entity.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DiaryMapper {

    /** 游标分页查询日记 */
    List<DiaryDTO> getDiaries(@Param("pageSize") Integer pageSize,
                              @Param("cursorId") Long cursorId,
                              @Param("cursorTime") LocalDateTime cursorTime,
                              @Param("userId") Long userId,
                              @Param("status") Integer status);

    /** 查询单条日记 */
    DiaryDTO getDiaryById(@Param("id") Long id,
                          @Param("status") Integer status);

    /** 写日记 */
    Integer insertDiary(Diary diary);

    /** 修改 / 软删除日记 */
    Integer updateDiary(Diary diary);
}
