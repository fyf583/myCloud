package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.dto.DiaryDTO;
import com.fyf.entity.Diary;
import com.fyf.enums.DiaryStatusEnum;
import com.fyf.exception.DiaryException;
import com.fyf.exception.LoginException;
import com.fyf.mapper.DiaryMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.DiaryService;
import com.fyf.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaryServiceImpl implements DiaryService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    @Resource
    private DiaryMapper diaryMapper;

    /**
     * 日记列表：游标分页（create_time desc, id desc）
     */
    @Override
    public CursorPageResult<DiaryDTO> diaryList(CursorPage cursorPage) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<DiaryDTO> list = diaryMapper.getDiaries(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, DiaryStatusEnum.NORMAL.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<DiaryDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            DiaryDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 查看日记
     */
    @Override
    public DiaryDTO getDiary(Long diaryId) {
        currentUserId();
        DiaryDTO diary = diaryMapper.getDiaryById(diaryId, DiaryStatusEnum.NORMAL.getCode());
        if (diary == null) {
            throw new DiaryException("日记不存在");
        }
        return diary;
    }

    /**
     * 写日记：userId 来自网关注入的登录态，status/createTime 由服务端填充
     */
    @Override
    public void createDiary(DiaryDTO diaryDTO) {
        Long userId = currentUserId();
        if (diaryDTO == null || diaryDTO.getDiary() == null || diaryDTO.getDiary().isBlank()) {
            throw new DiaryException("日记内容不能为空");
        }

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setDiary(diaryDTO.getDiary().trim());
        diary.setStatus(DiaryStatusEnum.NORMAL.getCode());

        if (diaryMapper.insertDiary(diary) != 1) {
            throw new DiaryException("写日记失败");
        }
    }

    /**
     * 修改日记
     */
    @Override
    public void updateDiary(Long diaryId, DiaryDTO diaryDTO) {
        currentUserId();
        if (diaryDTO == null || diaryDTO.getDiary() == null || diaryDTO.getDiary().isBlank()) {
            throw new DiaryException("日记内容不能为空");
        }
        if (diaryMapper.getDiaryById(diaryId, DiaryStatusEnum.NORMAL.getCode()) == null) {
            throw new DiaryException("日记不存在");
        }

        Diary diary = new Diary();
        diary.setId(diaryId);
        diary.setDiary(diaryDTO.getDiary().trim());

        if (diaryMapper.updateDiary(diary) != 1) {
            throw new DiaryException("修改日记失败");
        }
    }

    /**
     * 删除日记（软删除：status 置为已删除）
     */
    @Override
    public void deleteDiary(Long diaryId) {
        currentUserId();
        Diary diary = new Diary();
        diary.setId(diaryId);
        diary.setStatus(DiaryStatusEnum.DELETE.getCode());

        if (diaryMapper.updateDiary(diary) != 1) {
            throw new DiaryException("删除日记失败");
        }
    }

    /** 取当前登录用户（由网关 X-User-Id 透传），未登录抛异常 */
    private Long currentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new LoginException("请先登录");
        }
        return userId;
    }

    /** pageSize 兜底：空/非正数/超上限都按 20 处理 */
    private int resolvePageSize(CursorPage page) {
        Integer pageSize = page.getPageSize();
        if (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }
}
