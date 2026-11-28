package com.fyf.mapper;

import com.fyf.dto.NoteDTO;
import com.fyf.dto.NoteGroupDTO;
import com.fyf.entity.Note;
import com.fyf.entity.NoteGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NoteMapper {

    /** 游标分页查询笔记 */
    List<NoteDTO> getNotes(@Param("pageSize") Integer pageSize,
                           @Param("cursorId") Long cursorId,
                           @Param("cursorTime") LocalDateTime cursorTime,
                           @Param("userId") Long userId,
                           @Param("typeId") Long typeId,
                           @Param("status") Integer status);

    /** 查询单条笔记 */
    NoteDTO getNoteById(@Param("id") Long id,
                        @Param("status") Integer status);

    /** 新建笔记 */
    Integer insertNote(Note note);

    /** 修改笔记（内容 / 分组 / 状态） */
    Integer updateNote(Note note);

    /** 单独更新分组（typeId 允许为 null，用于移出分组） */
    Integer updateNoteGroup(@Param("id") Long id,
                            @Param("typeId") Long typeId,
                            @Param("updateTime") LocalDateTime updateTime);

    /** 游标分页查询分组 */
    List<NoteGroupDTO> getGroups(@Param("pageSize") Integer pageSize,
                                 @Param("cursorId") Long cursorId,
                                 @Param("cursorTime") LocalDateTime cursorTime,
                                 @Param("userId") Long userId,
                                 @Param("status") Integer status);

    /** 新建分组 */
    Integer insertGroup(NoteGroup group);

    /** 统计某用户某分组下的笔记数 */
    Integer countByTypeId(@Param("typeId") Long typeId,
                          @Param("userId") Long userId,
                          @Param("status") Integer status);
}
