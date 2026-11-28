package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.TodoDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.TodoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 待做日程模块（对应 myCloud.md 第 6 章）
 * <pre>
 * GET  /todo                 日程列表（游标分页，可选 status 过滤）
 * POST /todo                 新建日程
 * POST /todo/{todoId}        修改日程
 * POST /todo/done/{todoId}   完成日程
 * POST /todo/rm/{todoId}     删除日程
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/todo")
public class TodoController {

    @Resource
    private TodoService todoService;

    /** 日程列表（游标分页） */
    @GetMapping
    public Result<CursorPageResult<TodoDTO>> todoList(CursorPage cursorPage,
                                                      @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(todoService.todoList(cursorPage, status));
    }

    /** 新建日程 */
    @PostMapping
    public Result<Void> createTodo(@RequestBody TodoDTO todoDTO) {
        todoService.createTodo(todoDTO);
        return Result.success();
    }

    /** 修改日程 */
    @PostMapping("/{todoId}")
    public Result<Void> updateTodo(@PathVariable("todoId") Long todoId, @RequestBody TodoDTO todoDTO) {
        todoService.updateTodo(todoId, todoDTO);
        return Result.success();
    }

    /** 完成日程 */
    @PostMapping("/done/{todoId}")
    public Result<Void> doneTodo(@PathVariable("todoId") Long todoId) {
        todoService.markDone(todoId);
        return Result.success();
    }

    /** 删除日程（软删除） */
    @PostMapping("/rm/{todoId}")
    public Result<Void> deleteTodo(@PathVariable("todoId") Long todoId) {
        todoService.deleteTodo(todoId);
        return Result.success();
    }
}
