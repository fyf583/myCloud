package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.DiaryDTO;
import com.fyf.result.CursorPageResult;

public interface DiaryService {

    /** 日记列表（游标分页） */
    CursorPageResult<DiaryDTO> diaryList(CursorPage cursorPage);

    /** 查看日记 */
    DiaryDTO getDiary(Long diaryId);

    /** 写日记 */
    void createDiary(DiaryDTO diaryDTO);

    /** 修改日记 */
    void updateDiary(Long diaryId, DiaryDTO diaryDTO);

    /** 删除日记（软删除） */
    void deleteDiary(Long diaryId);
}
