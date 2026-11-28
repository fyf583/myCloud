package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.dto.SongDTO;
import com.fyf.entity.Song;
import com.fyf.enums.SongStatusEnum;
import com.fyf.exception.LoginException;
import com.fyf.exception.SongException;
import com.fyf.mapper.SongMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.SongService;
import com.fyf.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SongServiceImpl implements SongService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    /** 歌曲名称最大长度（与 song.song_name 一致） */
    private static final int MAX_SONG_NAME_LENGTH = 100;

    /** 歌手最大长度（与 song.singer 一致） */
    private static final int MAX_SINGER_LENGTH = 50;

    /** 音频地址最大长度（与 song.url 一致） */
    private static final int MAX_URL_LENGTH = 500;

    @Resource
    private SongMapper songMapper;

    /**
     * 歌单列表：游标分页（create_time desc, id desc）
     */
    @Override
    public CursorPageResult<SongDTO> songList(CursorPage cursorPage) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<SongDTO> list = songMapper.getSongs(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, SongStatusEnum.NORMAL.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<SongDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            SongDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 添加歌曲：songName 必填、url 必填且必须是 http(s) 地址
     */
    @Override
    public void addSong(SongDTO songDTO) {
        Long userId = currentUserId();
        if (songDTO == null) {
            throw new SongException("歌曲信息不能为空");
        }

        String songName = validateSongName(songDTO.getSongName());
        String singer = resolveSinger(songDTO.getSinger());
        String url = validateUrl(songDTO.getUrl());

        Song song = new Song();
        song.setUserId(userId);
        song.setSongName(songName);
        song.setSinger(singer);
        song.setUrl(url);
        song.setStatus(SongStatusEnum.NORMAL.getCode());

        if (songMapper.insertSong(song) != 1) {
            throw new SongException("添加歌曲失败");
        }
    }

    /**
     * 播放歌曲：返回歌曲信息（前端用它拿音频地址）
     */
    @Override
    public SongDTO getSong(Long songId) {
        Long userId = currentUserId();
        SongDTO song = songMapper.getSongById(songId, userId, SongStatusEnum.NORMAL.getCode());
        if (song == null) {
            throw new SongException("歌曲不存在或已删除");
        }
        return song;
    }

    /**
     * 删除歌曲（软删除）
     */
    @Override
    public void deleteSong(Long songId) {
        Long userId = currentUserId();
        Song song = new Song();
        song.setId(songId);
        song.setStatus(SongStatusEnum.DELETE.getCode());

        if (songMapper.updateSong(song, userId) != 1) {
            throw new SongException("歌曲不存在或已删除");
        }
    }

    /** 歌曲名校验 */
    private String validateSongName(String songName) {
        if (songName == null || songName.isBlank()) {
            throw new SongException("歌曲名称不能为空");
        }
        String trimmed = songName.trim();
        if (trimmed.length() > MAX_SONG_NAME_LENGTH) {
            throw new SongException("歌曲名称不能超过 " + MAX_SONG_NAME_LENGTH + " 个字符");
        }
        return trimmed;
    }

    /** 歌手：可空，超长报错 */
    private String resolveSinger(String singer) {
        if (singer == null || singer.isBlank()) {
            return null;
        }
        String trimmed = singer.trim();
        if (trimmed.length() > MAX_SINGER_LENGTH) {
            throw new SongException("歌手名称不能超过 " + MAX_SINGER_LENGTH + " 个字符");
        }
        return trimmed;
    }

    /** 音频地址：必填，且必须是 http(s) 链接 */
    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new SongException("音频地址不能为空");
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new SongException("音频地址必须以 http:// 或 https:// 开头");
        }
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw new SongException("音频地址不能超过 " + MAX_URL_LENGTH + " 个字符");
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
