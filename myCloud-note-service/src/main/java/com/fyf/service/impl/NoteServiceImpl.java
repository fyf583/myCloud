package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.dto.NoteDTO;
import com.fyf.dto.NoteGroupDTO;
import com.fyf.entity.Note;
import com.fyf.entity.NoteGroup;
import com.fyf.enums.NoteStatusEnum;
import com.fyf.exception.LoginException;
import com.fyf.exception.NoteException;
import com.fyf.mapper.NoteMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.NoteService;
import com.fyf.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    @Resource
    private NoteMapper noteMapper;

    /**
     * 笔记缩略：游标分页（create_time desc, id desc）
     */
    @Override
    public CursorPageResult<NoteDTO> noteList(CursorPage cursorPage, Long typeId) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<NoteDTO> list = noteMapper.getNotes(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, typeId, NoteStatusEnum.NORMAL.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<NoteDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            NoteDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 查看笔记
     */
    @Override
    public NoteDTO getNote(Long noteId) {
        currentUserId();
        NoteDTO note = noteMapper.getNoteById(noteId, NoteStatusEnum.NORMAL.getCode());
        if (note == null) {
            throw new NoteException("笔记不存在");
        }
        return note;
    }

    /**
     * 新建笔记：userId 来自网关注入的登录态，status/createTime 由服务端填充
     */
    @Override
    public void createNote(NoteDTO noteDTO) {
        Long userId = currentUserId();
        if (noteDTO == null || noteDTO.getNote() == null || noteDTO.getNote().isBlank()) {
            throw new NoteException("笔记内容不能为空");
        }

        Note note = new Note();
        note.setUserId(userId);
        note.setNote(noteDTO.getNote().trim());
        note.setTypeId(noteDTO.getTypeId());
        note.setStatus(NoteStatusEnum.NORMAL.getCode());

        if (noteMapper.insertNote(note) != 1) {
            throw new NoteException("新建笔记失败");
        }
    }

    /**
     * 修改笔记内容
     */
    @Override
    public void updateNote(Long noteId, NoteDTO noteDTO) {
        currentUserId();
        if (noteDTO == null || noteDTO.getNote() == null || noteDTO.getNote().isBlank()) {
            throw new NoteException("笔记内容不能为空");
        }
        if (noteMapper.getNoteById(noteId, NoteStatusEnum.NORMAL.getCode()) == null) {
            throw new NoteException("笔记不存在");
        }

        Note note = new Note();
        note.setId(noteId);
        note.setNote(noteDTO.getNote().trim());

        if (noteMapper.updateNote(note) != 1) {
            throw new NoteException("修改笔记失败");
        }
    }

    /**
     * 笔记分组：typeId 为空表示移出分组
     */
    @Override
    public void moveToGroup(Long noteId, Long typeId) {
        currentUserId();
        if (noteMapper.getNoteById(noteId, NoteStatusEnum.NORMAL.getCode()) == null) {
            throw new NoteException("笔记不存在");
        }
        if (noteMapper.updateNoteGroup(noteId, typeId, LocalDateTime.now()) != 1) {
            throw new NoteException("笔记分组失败");
        }
    }

    /**
     * 删除笔记（软删除：status 置为已删除）
     */
    @Override
    public void deleteNote(Long noteId) {
        currentUserId();
        Note note = new Note();
        note.setId(noteId);
        note.setStatus(NoteStatusEnum.DELETE.getCode());

        if (noteMapper.updateNote(note) != 1) {
            throw new NoteException("删除笔记失败");
        }
    }

    /**
     * 新建分组
     */
    @Override
    public void createGroup(NoteGroupDTO groupDTO) {
        Long userId = currentUserId();
        if (groupDTO == null || groupDTO.getGroupName() == null || groupDTO.getGroupName().isBlank()) {
            throw new NoteException("分组名称不能为空");
        }

        NoteGroup group = new NoteGroup();
        group.setUserId(userId);
        group.setGroupName(groupDTO.getGroupName().trim());
        group.setStatus(NoteStatusEnum.NORMAL.getCode());

        if (noteMapper.insertGroup(group) != 1) {
            throw new NoteException("新建分组失败");
        }
    }

    /**
     * 分组列表：游标分页
     */
    @Override
    public CursorPageResult<NoteGroupDTO> groupList(CursorPage cursorPage) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<NoteGroupDTO> list = noteMapper.getGroups(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, NoteStatusEnum.NORMAL.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<NoteGroupDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            NoteGroupDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
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
