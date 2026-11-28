package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.NoteDTO;
import com.fyf.dto.NoteGroupDTO;
import com.fyf.result.CursorPageResult;

public interface NoteService {

    /** 笔记缩略（游标分页），typeId 为空表示全部 */
    CursorPageResult<NoteDTO> noteList(CursorPage cursorPage, Long typeId);

    /** 查看笔记详情 */
    NoteDTO getNote(Long noteId);

    /** 新建笔记 */
    void createNote(NoteDTO noteDTO);

    /** 修改笔记 */
    void updateNote(Long noteId, NoteDTO noteDTO);

    /** 笔记分组（typeId 为 null 表示移出分组） */
    void moveToGroup(Long noteId, Long typeId);

    /** 删除笔记（软删除） */
    void deleteNote(Long noteId);

    /** 新建分组 */
    void createGroup(NoteGroupDTO groupDTO);

    /** 分组列表（游标分页） */
    CursorPageResult<NoteGroupDTO> groupList(CursorPage cursorPage);
}
