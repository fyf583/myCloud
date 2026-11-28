package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.SongDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.SongService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 我的歌单模块（对应 myCloud.md 第 8 章）
 * <pre>
 * GET  /song              歌单列表（游标分页）
 * POST /song              添加歌曲
 * GET  /song/{songId}     播放歌曲（返回歌曲信息，含音频地址）
 * POST /song/rm/{songId}  删除歌曲
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/song")
public class SongController {

    @Resource
    private SongService songService;

    /** 歌单列表（游标分页） */
    @GetMapping
    public Result<CursorPageResult<SongDTO>> songList(CursorPage cursorPage) {
        return Result.success(songService.songList(cursorPage));
    }

    /** 添加歌曲 */
    @PostMapping
    public Result<Void> addSong(@RequestBody SongDTO songDTO) {
        songService.addSong(songDTO);
        return Result.success();
    }

    /** 播放歌曲（返回歌曲信息） */
    @GetMapping("/{songId}")
    public Result<SongDTO> getSong(@PathVariable("songId") Long songId) {
        return Result.success(songService.getSong(songId));
    }

    /** 删除歌曲（软删除） */
    @PostMapping("/rm/{songId}")
    public Result<Void> deleteSong(@PathVariable("songId") Long songId) {
        songService.deleteSong(songId);
        return Result.success();
    }
}
