package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.NoteDTO;
import com.fyf.dto.NoteGroupDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记模块（对应 myCloud.md 第 3 章）
 * <pre>
 * GET  /note                          笔记缩略（游标分页，可选 typeId 过滤分组）
 * GET  /note/group                    分组列表（文档未定义，前端需要，已补充）
 * GET  /note/{noteId}                 查看笔记
 * POST /note                          新建笔记
 * POST /note/group                    新建分组
 * POST /note/{noteId}                 修改笔记
 * POST /note/{noteId}/group?typeId=   笔记分组
 * POST /note/rm/{noteId}              删除笔记
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/note")
public class NoteController {

    @Resource
    private NoteService noteService;

    /** 笔记缩略（游标分页） */
    @GetMapping
    public Result<CursorPageResult<NoteDTO>> noteList(CursorPage cursorPage,
                                                      @RequestParam(value = "typeId", required = false) Long typeId) {
        return Result.success(noteService.noteList(cursorPage, typeId));
    }

    /** 分组列表（精确路径优先于 /note/{noteId} 匹配） */
    @GetMapping("/group")
    public Result<CursorPageResult<NoteGroupDTO>> groupList(CursorPage cursorPage) {
        return Result.success(noteService.groupList(cursorPage));
    }

    /** 查看笔记 */
    @GetMapping("/{noteId}")
    public Result<NoteDTO> getNote(@PathVariable("noteId") Long noteId) {
        return Result.success(noteService.getNote(noteId));
    }

    /** 新建笔记 */
    @PostMapping
    public Result<Void> createNote(@RequestBody NoteDTO noteDTO) {
        noteService.createNote(noteDTO);
        return Result.success();
    }

    /** 新建分组 */
    @PostMapping("/group")
    public Result<Void> createGroup(@RequestBody NoteGroupDTO groupDTO) {
        noteService.createGroup(groupDTO);
        return Result.success();
    }

    /** 修改笔记 */
    @PostMapping("/{noteId}")
    public Result<Void> updateNote(@PathVariable("noteId") Long noteId, @RequestBody NoteDTO noteDTO) {
        noteService.updateNote(noteId, noteDTO);
        return Result.success();
    }

    /** 笔记分组（typeId 为空表示移出分组） */
    @PostMapping("/{noteId}/group")
    public Result<Void> moveToGroup(@PathVariable("noteId") Long noteId,
                                    @RequestParam(value = "typeId", required = false) Long typeId) {
        noteService.moveToGroup(noteId, typeId);
        return Result.success();
    }

    /** 删除笔记（软删除） */
    @PostMapping("/rm/{noteId}")
    public Result<Void> deleteNote(@PathVariable("noteId") Long noteId) {
        noteService.deleteNote(noteId);
        return Result.success();
    }
}
