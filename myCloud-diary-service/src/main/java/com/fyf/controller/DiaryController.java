package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.DiaryDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.DiaryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 日记模块（对应 myCloud.md 第 4 章）
 * <pre>
 * GET  /diary                 日记列表（游标分页）
 * GET  /diary/{diaryId}       查看日记
 * POST /diary                 写日记
 * POST /diary/{diaryId}       修改日记
 * POST /diary/rm/{diaryId}    删除日记
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/diary")
public class DiaryController {

    @Resource
    private DiaryService diaryService;

    /** 日记列表（游标分页） */
    @GetMapping
    public Result<CursorPageResult<DiaryDTO>> diaryList(CursorPage cursorPage) {
        return Result.success(diaryService.diaryList(cursorPage));
    }

    /** 查看日记 */
    @GetMapping("/{diaryId}")
    public Result<DiaryDTO> getDiary(@PathVariable("diaryId") Long diaryId) {
        return Result.success(diaryService.getDiary(diaryId));
    }

    /** 写日记 */
    @PostMapping
    public Result<Void> createDiary(@RequestBody DiaryDTO diaryDTO) {
        diaryService.createDiary(diaryDTO);
        return Result.success();
    }

    /** 修改日记 */
    @PostMapping("/{diaryId}")
    public Result<Void> updateDiary(@PathVariable("diaryId") Long diaryId, @RequestBody DiaryDTO diaryDTO) {
        diaryService.updateDiary(diaryId, diaryDTO);
        return Result.success();
    }

    /** 删除日记（软删除） */
    @PostMapping("/rm/{diaryId}")
    public Result<Void> deleteDiary(@PathVariable("diaryId") Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return Result.success();
    }
}
