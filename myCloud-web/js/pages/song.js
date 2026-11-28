/**
 * pages/song.js —— 我的歌单
 * 接口：GET /song（列表）、POST /song（添加）、GET /song/{songId}（播放地址）、POST /song/rm/{songId}（删除）
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('song');

  const $ = function (id) { return document.getElementById(id); };
  const listEl = $('songList');
  const moreWrap = $('loadMoreWrap');

  const state = { items: [], cursorId: null, cursorTime: null, hasMore: true, loading: false };

  function renderList() {
    if (!state.items.length) {
      listEl.innerHTML = UI.emptyHTML(state.loading ? '加载中…' : '歌单还是空的，添加一首吧', '🎵');
      return;
    }
    listEl.innerHTML = state.items.map(function (s) {
      const url = s.url && s.url !== '#' ? s.url : '';
      return '' +
        '<div class="audio-item">' +
          '<div style="flex:0 0 36px;height:36px;display:grid;place-items:center;border-radius:8px;background:#eff6ff;font-size:16px">🎵</div>' +
          '<div style="flex:1;min-width:0">' +
            '<div class="list-item-title" style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' +
              UI.escapeHtml(s.songName || '未命名') + '</div>' +
            '<div class="list-item-meta">' + UI.escapeHtml(s.singer || '未知歌手') +
              ' · ' + UI.escapeHtml(UI.formatTime(s.createTime)) + '</div>' +
          '</div>' +
          (url
            ? '<audio controls preload="none" src="' + UI.escapeHtml(url) + '"></audio>'
            : '<span class="text-sub" style="font-size:12px">无音频地址</span>') +
          '<button class="btn-text danger btn-sm" data-del="' + s.id + '" type="button">删除</button>' +
        '</div>';
    }).join('');
  }

  function renderMore(mode) {
    if (mode === 'loading') {
      moreWrap.innerHTML = '<div class="loading">加载中</div>';
    } else if (mode === 'more') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnMore" type="button">加载更多</button>';
      $('btnMore').addEventListener('click', function () { load(false); });
    } else if (mode === 'end') {
      moreWrap.innerHTML = state.items.length ? '<div class="no-more">— 没有更多了 —</div>' : '';
    } else if (mode === 'error') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnRetry" type="button">加载失败，点击重试</button>';
      $('btnRetry').addEventListener('click', function () { load(true); });
    }
  }

  async function load(reset) {
    if (state.loading) return;
    state.loading = true;
    if (reset) { state.cursorId = null; state.cursorTime = null; state.hasMore = true; }
    renderMore('loading');

    const payload = { pageSize: CONFIG.PAGE_SIZE };
    if (!reset && state.cursorId) {
      payload.cursorId = state.cursorId;
      payload.cursorTime = state.cursorTime;
    }

    try {
      const data = await Http.get(CONFIG.API.song, { query: payload });
      const page = Http.normalizePage(data);
      state.items = reset ? page.list : state.items.concat(page.list);
      state.hasMore = page.hasMore;
      state.cursorId = page.cursorId;
      state.cursorTime = page.cursorTime;
      renderList();
      renderMore(state.hasMore ? 'more' : 'end');
    } catch (e) {
      renderMore('error');
      if (!state.items.length) listEl.innerHTML = UI.emptyHTML('加载失败：' + (e.message || ''), '⚠️');
    } finally {
      state.loading = false;
    }
  }

  /* ---------------- 添加歌曲 ---------------- */
  $('btnNewSong').addEventListener('click', function () {
    UI.modal({
      title: '添加歌曲',
      body:
        '<div class="form-item"><label class="form-label">歌曲名称<span class="required">*</span></label>' +
        '<input class="input" id="songName" maxlength="60" placeholder="例如：晴天"></div>' +
        '<div class="form-item"><label class="form-label">歌手</label>' +
        '<input class="input" id="songSinger" maxlength="40" placeholder="选填"></div>' +
        '<div class="form-item"><label class="form-label">音频地址<span class="required">*</span></label>' +
        '<input class="input" id="songUrl" placeholder="例如：https://example.com/a.mp3"></div>' +
        '<p class="form-tip">当前为纯前端演示，音频需是可公开访问的 mp3 / m4a 链接</p>',
      okText: '添加',
      onOk: async function (bodyEl) {
        const songName = bodyEl.querySelector('#songName').value.trim();
        const singer = bodyEl.querySelector('#songSinger').value.trim();
        const url = bodyEl.querySelector('#songUrl').value.trim();
        if (!songName) { UI.toast('请填写歌曲名称', 'warning'); return false; }
        if (!url) { UI.toast('请填写音频地址', 'warning'); return false; }

        await Http.post(CONFIG.API.song, { songName: songName, singer: singer, url: url });
        UI.toast('添加成功', 'success');
        await load(true);
      }
    });
  });

  /* ---------------- 删除 ---------------- */
  listEl.addEventListener('click', async function (event) {
    const delBtn = event.target.closest('[data-del]');
    if (!delBtn) return;
    const id = delBtn.getAttribute('data-del');
    const ok = await UI.confirm('确定从歌单移除这首歌吗？');
    if (!ok) return;

    await Http.post(CONFIG.API.song + '/rm/' + id);
    UI.toast('已删除', 'success');
    state.items = state.items.filter(function (s) { return String(s.id) !== String(id); });
    renderList();
  });

  $('btnRefresh').addEventListener('click', function () { load(true); });

  load(true);
})();
