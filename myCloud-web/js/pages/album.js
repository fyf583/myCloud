/**
 * pages/album.js —— 相册
 * 接口：GET /album（列表）、POST /album（上传，multipart/form-data）、
 *       GET /album/{photoId}（查看）、POST /album/rm/{photoId}（删除）
 * 注意：后端返回的 url 是相对网关的路径（/album/file/xxx.jpg），渲染前要拼上 CONFIG.BASE_URL
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('album');

  const $ = function (id) { return document.getElementById(id); };
  const grid = $('photoGrid');
  const moreWrap = $('loadMoreWrap');

  const state = { items: [], cursorId: null, cursorTime: null, hasMore: true, loading: false };

  /** 相对路径补全为网关绝对地址；已是完整 URL / data: 的原样返回 */
  function absoluteUrl(url) {
    if (!url) return '';
    if (/^(https?:)?\/\//i.test(url) || url.indexOf('data:') === 0) return url;
    return CONFIG.BASE_URL + (url.charAt(0) === '/' ? url : '/' + url);
  }

  function renderList() {
    if (!state.items.length) {
      grid.innerHTML = UI.emptyHTML('相册还没有照片', '🖼️');
      return;
    }
    grid.innerHTML = state.items.map(function (p) {
      const url = UI.escapeHtml(absoluteUrl(p.url));
      return '' +
        '<div class="photo-card" data-id="' + p.id + '" data-view="' + url + '" data-title="' + UI.escapeHtml(p.photoName || '') + '">' +
          '<img src="' + url + '" alt="' + UI.escapeHtml(p.photoName || '') + '"' +
               ' style="width:100%;height:130px;object-fit:cover;background:#eff6ff" ' +
               'onerror="this.remove()">' +
          '<button class="del" data-del="' + p.id + '" type="button" title="删除">&times;</button>' +
          '<div class="meta">' + UI.escapeHtml(p.photoName || '未命名') + '</div>' +
          '<div class="meta" style="padding-top:0;color:#9ca3af">' + UI.escapeHtml(UI.formatTime(p.createTime)) + '</div>' +
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
      const data = await Http.get(CONFIG.API.album, { query: payload });
      const page = Http.normalizePage(data);
      state.items = reset ? page.list : state.items.concat(page.list);
      state.hasMore = page.hasMore;
      state.cursorId = page.cursorId;
      state.cursorTime = page.cursorTime;
      renderList();
      renderMore(state.hasMore ? 'more' : 'end');
    } catch (e) {
      renderMore('error');
      if (!state.items.length) grid.innerHTML = UI.emptyHTML('加载失败：' + (e.message || ''), '⚠️');
    } finally {
      state.loading = false;
    }
  }

  /* ---------------- 上传 ---------------- */
  $('btnUpload').addEventListener('click', function () {
    UI.modal({
      title: '上传照片',
      body:
        '<div class="form-item"><label class="form-label">照片文件<span class="required">*</span></label>' +
        '<input class="input" type="file" id="photoFile" accept="image/*"></div>' +
        '<div class="form-item"><label class="form-label">照片名称</label>' +
        '<input class="input" id="photoName" maxlength="50" placeholder="留空则使用文件名"></div>' +
        '<div class="form-item"><label class="form-label">描述</label>' +
        '<input class="input" id="photoDesc" maxlength="100" placeholder="选填"></div>' +
        '<p class="form-tip">内容类型为 multipart/form-data，字段：file / photoName / description</p>',
      okText: '上传',
      onOk: async function (bodyEl) {
        const file = bodyEl.querySelector('#photoFile').files[0];
        if (!file) { UI.toast('请选择照片文件', 'warning'); return false; }

        const form = new FormData();
        form.append('file', file);
        form.append('photoName', bodyEl.querySelector('#photoName').value.trim() || file.name);
        form.append('description', bodyEl.querySelector('#photoDesc').value.trim());

        await Http.postForm(CONFIG.API.album, form);
        UI.toast('上传成功', 'success');
        await load(true);
      }
    });
  });

  /* ---------------- 查看 / 删除 ---------------- */
  grid.addEventListener('click', async function (event) {
    const delBtn = event.target.closest('[data-del]');
    if (delBtn) {
      const id = delBtn.getAttribute('data-del');
      const ok = await UI.confirm('确定删除这张照片吗？');
      if (!ok) return;
      await Http.post(CONFIG.API.album + '/rm/' + id);
      UI.toast('删除成功', 'success');
      state.items = state.items.filter(function (p) { return String(p.id) !== String(id); });
      renderList();
      return;
    }

    const card = event.target.closest('[data-view]');
    if (!card) return;
    const url = card.getAttribute('data-view');
    const title = card.getAttribute('data-title');
    UI.modal({
      title: title || '查看照片',
      body: '<img src="' + UI.escapeHtml(url) + '" alt="" style="width:100%;border-radius:8px">',
      okText: '关闭',
      showCancel: false
    });
  });

  $('btnRefresh').addEventListener('click', function () { load(true); });

  load(true);
})();
