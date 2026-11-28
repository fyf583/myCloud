package com.fyf.mapper;

import com.fyf.dto.TodoDTO;
import com.fyf.entity.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoMapper {

    /** 游标分页查询日程（status 为可选过滤条件，deleteStatus 用于排除已删除） */
    List<TodoDTO> getTodos(@Param("pageSize") Integer pageSize,
                           @Param("cursorId") Long cursorId,
                           @Param("cursorTime") LocalDateTime cursorTime,
                           @Param("userId") Long userId,
                           @Param("status") Integer status,
                           @Param("deleteStatus") Integer deleteStatus);

    /** 查询单条日程（带 userId，防止越权） */
    TodoDTO getTodoById(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("deleteStatus") Integer deleteStatus);

    /** 新建日程 */
    Integer insertTodo(Todo todo);

    /** 修改内容与截止时间（deadline 允许被清空，所以这里不做 null 判断） */
    Integer updateTodoContent(@Param("todo") Todo todo,
                              @Param("userId") Long userId,
                              @Param("deleteStatus") Integer deleteStatus);

    /** 修改状态（完成 / 删除） */
    Integer updateTodoStatus(@Param("todo") Todo todo,
                             @Param("userId") Long userId,
                             @Param("deleteStatus") Integer deleteStatus);
}
