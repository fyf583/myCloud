package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.dto.TodoDTO;
import com.fyf.entity.Todo;
import com.fyf.enums.TodoStatusEnum;
import com.fyf.exception.LoginException;
import com.fyf.exception.TodoException;
import com.fyf.mapper.TodoMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.TodoService;
import com.fyf.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    /** 待做内容最大长度（与 todo.content 一致） */
    private static final int MAX_CONTENT_LENGTH = 200;

    @Resource
    private TodoMapper todoMapper;

    /**
     * 日程列表：游标分页（create_time desc, id desc），status 可选 0/1
     */
    @Override
    public CursorPageResult<TodoDTO> todoList(CursorPage cursorPage, Integer status) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);
        // 只有 0/1 是合法的“可见状态”，其它值（含 2 已删除）按“全部”处理
        Integer statusFilter = TodoStatusEnum.isVisible(status) ? status : null;

        List<TodoDTO> list = todoMapper.getTodos(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, statusFilter, TodoStatusEnum.DELETE.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<TodoDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            TodoDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 新建日程：userId 来自网关注入的登录态，status/createTime 由服务端填充
     */
    @Override
    public void createTodo(TodoDTO todoDTO) {
        Long userId = currentUserId();
        String content = validateContent(todoDTO == null ? null : todoDTO.getContent());

        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setContent(content);
        todo.setDeadline(todoDTO.getDeadline());
        todo.setStatus(TodoStatusEnum.UNDONE.getCode());

        if (todoMapper.insertTodo(todo) != 1) {
            throw new TodoException("新建日程失败");
        }
    }

    /**
     * 修改日程：只改内容与截止时间，状态统一走 /todo/done
     */
    @Override
    public void updateTodo(Long todoId, TodoDTO todoDTO) {
        Long userId = currentUserId();
        String content = validateContent(todoDTO == null ? null : todoDTO.getContent());
        getExisting(todoId, userId);

        Todo todo = new Todo();
        todo.setId(todoId);
        todo.setContent(content);
        // 允许把截止时间清空，所以这里直接使用前端值（可能为 null）
        todo.setDeadline(todoDTO.getDeadline());

        if (todoMapper.updateTodoContent(todo, userId, TodoStatusEnum.DELETE.getCode()) != 1) {
            throw new TodoException("修改日程失败");
        }
    }

    /**
     * 完成日程
     */
    @Override
    public void markDone(Long todoId) {
        Long userId = currentUserId();
        Todo todo = new Todo();
        todo.setId(todoId);
        todo.setStatus(TodoStatusEnum.DONE.getCode());

        if (todoMapper.updateTodoStatus(todo, userId, TodoStatusEnum.DELETE.getCode()) != 1) {
            throw new TodoException("日程不存在或已删除");
        }
    }

    /**
     * 删除日程（软删除：status 置为已删除）
     */
    @Override
    public void deleteTodo(Long todoId) {
        Long userId = currentUserId();
        Todo todo = new Todo();
        todo.setId(todoId);
        todo.setStatus(TodoStatusEnum.DELETE.getCode());

        if (todoMapper.updateTodoStatus(todo, userId, TodoStatusEnum.DELETE.getCode()) != 1) {
            throw new TodoException("日程不存在或已删除");
        }
    }

    /** 校验并在日程不存在/已删除时抛异常 */
    private TodoDTO getExisting(Long todoId, Long userId) {
        TodoDTO todo = todoMapper.getTodoById(todoId, userId, TodoStatusEnum.DELETE.getCode());
        if (todo == null) {
            throw new TodoException("日程不存在或已删除");
        }
        return todo;
    }

    /** 内容校验：非空 + 长度上限 */
    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new TodoException("待做内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new TodoException("待做内容不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }
        return trimmed;
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
