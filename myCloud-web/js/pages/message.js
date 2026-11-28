/**
 * pages/message.js —— 留言板
 * 对应接口（myCloud.md 第 2 章）：
 *   GET  /message                 显示留言（游标分页）
 *   POST /message                 发布留言
 *   POST /message/{messageId}     删除留言（软删除）
 *   GET  /message/{userId}        查询某用户的留言（游标分页）
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('message');

  const $ = function (id) { return document.getElementById(id); };
  const listEl = $('messageList');
  const moreWrap = $('loadMoreWrap');
  const input = $('messageInput');

  const state = {
    items: [],
    cursorId: null,
    cursorTime: null,
    hasMore: true,
    loading: false,
    userId: null
  };

  function pagePath() {
    return state.userId ? CONFIG.API.message + '/' + state.userId : CONFIG.API.message;
  }

  /* ---------------- 渲染 ---------------- */
  function renderList() {
    if (!state.items.length) {
      listEl.innerHTML = UI.emptyHTML('还没有留言，说点什么吧～', '💬');
      return;
    }
    listEl.innerHTML = state.items.map(function (item) {
      return '' +
        '<div class="list-item" data-id="' + item.id + '">' +
          '<div class="list-item-main">' +
            '<div class="list-item-title">' + UI.escapeHtml(item.message) + '</div>' +
            '<button class="btn-text danger btn-sm" data-del="' + item.id + '" type="button">删除</button>' +
          '</div>' +
          '<div class="list-item-meta">#' + item.id + ' · ' + UI.escapeHtml(UI.formatTime(item.createTime)) + '</div>' +
        '</div>';
    }).join('');
  }

  function renderMore(mode) {
    if (mode === 'loading') {
      moreWrap.innerHTML = '<div class="loading">加载中</div>';
    } else if (mode === 'more') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnMore" type="button">加载更多</button>';
      $('btnMore').addEventListener('click', function () { loadPage(false); });
    } else if (mode === 'end') {
      moreWrap.innerHTML = state.items.length ? '<div class="no-more">— 没有更多了 —</div>' : '';
    } else if (mode === 'error') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnRetry" type="button">加载失败，点击重试</button>';
      $('btnRetry').addEventListener('click', function () { loadPage(true); });
    }
  }

  /* ---------------- 数据 ---------------- */
  async function loadPage(reset) {
    if (state.loading) return;
    state.loading = true;
    if (reset) {
      state.cursorId = null;
      state.cursorTime = null;
      state.hasMore = true;
    }
    renderMore('loading');

    const payload = { pageSize: CONFIG.PAGE_SIZE };
    if (!reset && state.cursorId) {
      payload.cursorId = state.cursorId;
      payload.cursorTime = state.cursorTime;
    }

    try {
      // 注意：后端 MessageController 用 @RequestBody 接收分页参数，所以这里用 data（GET 带 body）。
      // 若后端后续改成 Query 参数，把 { query: payload } 换成 { query: payload } 即可。
      const data = await Http.get(pagePath(), { query: payload });
      const page = Http.normalizePage(data);

      state.items = reset ? page.list : state.items.concat(page.list);
      state.hasMore = page.hasMore;
      state.cursorId = page.cursorId;
      state.cursorTime = page.cursorTime;

      renderList();
      renderMore(state.hasMore ? 'more' : 'end');
    } catch (e) {
      renderMore('error');
      if (!state.items.length) listEl.innerHTML = UI.emptyHTML('加载失败：' + (e.message || '未知错误'), '⚠️');
    } finally {
      state.loading = false;
    }
  }

  /* ---------------- 发布 ---------------- */
  $('btnSend').addEventListener('click', async function () {
    const message = input.value.trim();
    if (!message) return UI.toast('留言内容不能为空', 'warning');
    if (message.length > 500) return UI.toast('留言最多 500 字', 'warning');

    const btn = $('btnSend');
    const restore = UI.btnLoading(btn, '发布中');
    try {
      // 后端只接收 message：userId 由网关透传的 X-User-Id 解析，status/createTime 由服务端填充
      await Http.post(CONFIG.API.message, { message: message });
      UI.toast('发布成功', 'success');
      input.value = '';
      updateCount();
      state.items = [];
      await loadPage(true);
    } catch (e) {
      /* 错误提示已在 request.js 中统一 toast */
    } finally {
      restore();
    }
  });

  /* ---------------- 删除 ---------------- */
  listEl.addEventListener('click', async function (event) {
    const btn = event.target.closest('[data-del]');
    if (!btn) return;
    const id = btn.getAttribute('data-del');

    const ok = await UI.confirm('确定删除这条留言吗？（软删除，管理员可恢复）');
    if (!ok) return;

    try {
      await Http.post(CONFIG.API.message + '/' + id);
      UI.toast('删除成功', 'success');
      state.items = state.items.filter(function (item) { return String(item.id) !== String(id); });
      renderList();
      if (!state.items.length) renderMore('loading'), loadPage(true);
    } catch (e) { /* 已 toast */ }
  });

  /* ---------------- 筛选 / 刷新 ---------------- */
  $('btnFilter').addEventListener('click', function () {
    const raw = $('filterUserId').value.trim();
    if (raw && !/^\d+$/.test(raw)) return UI.toast('用户ID 必须是数字', 'warning');
    state.userId = raw || null;
    $('listTitle').textContent = state.userId ? ('用户 ' + state.userId + ' 的留言') : '全部留言';
    state.items = [];
    loadPage(true);
  });

  $('filterUserId').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') $('btnFilter').click();
  });

  $('btnRefresh').addEventListener('click', function () {
    state.items = [];
    loadPage(true);
  });

  /* ---------------- 字数统计 ---------------- */
  function updateCount() {
    $('charCount').textContent = input.value.length;
  }
  input.addEventListener('input', updateCount);

  /* ---------------- 初始化 ---------------- */
  loadPage(true);
})();
