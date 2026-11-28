package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.TodoDTO;
import com.fyf.result.CursorPageResult;

public interface TodoService {

    /** 日程列表（游标分页，status 可选：0 未完成、1 已完成，不传则全部） */
    CursorPageResult<TodoDTO> todoList(CursorPage cursorPage, Integer status);

    /** 新建日程 */
    void createTodo(TodoDTO todoDTO);

    /** 修改日程（内容 + 截止时间） */
    void updateTodo(Long todoId, TodoDTO todoDTO);

    /** 标记完成 */
    void markDone(Long todoId);

    /** 删除日程（软删除） */
    void deleteTodo(Long todoId);
}
