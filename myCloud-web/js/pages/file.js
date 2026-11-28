/**
 * pages/file.js —— 文件分享
 * 接口：GET /file（列表）、POST /file（上传，multipart/form-data）、
 *       GET /file/{fileId}（下载地址）、POST /file/rm/{fileId}（删除）
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('file');

  const $ = function (id) { return document.getElementById(id); };
  const tbody = $('fileTableBody');
  const moreWrap = $('loadMoreWrap');

  const state = { items: [], cursorId: null, cursorTime: null, hasMore: true, loading: false };

  function formatSize(bytes) {
    const size = Number(bytes) || 0;
    if (size < 1024) return size + ' B';
    if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
    return (size / 1024 / 1024).toFixed(2) + ' MB';
  }

  function renderList() {
    if (!state.items.length) {
      tbody.innerHTML = '<tr><td colspan="6">' + UI.emptyHTML(state.loading ? '加载中…' : '暂无文件', '📁') + '</td></tr>';
      return;
    }
    tbody.innerHTML = state.items.map(function (f) {
      return '' +
        '<tr>' +
          '<td class="text-sub">' + f.id + '</td>' +
          '<td>' + UI.escapeHtml(f.fileName || '未命名') + '</td>' +
          '<td class="text-sub">' + UI.escapeHtml(f.description || '—') + '</td>' +
          '<td class="text-sub">' + formatSize(f.size) + '</td>' +
          '<td class="text-sub">' + UI.escapeHtml(UI.formatTime(f.createTime)) + '</td>' +
          '<td>' +
            '<div class="actions">' +
              '<button class="btn-text btn-sm" data-download="' + f.id + '" type="button">下载</button>' +
              '<button class="btn-text danger btn-sm" data-del="' + f.id + '" type="button">删除</button>' +
            '</div>' +
          '</td>' +
        '</tr>';
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
      const data = await Http.get(CONFIG.API.file, { query: payload });
      const page = Http.normalizePage(data);
      state.items = reset ? page.list : state.items.concat(page.list);
      state.hasMore = page.hasMore;
      state.cursorId = page.cursorId;
      state.cursorTime = page.cursorTime;
      renderList();
      renderMore(state.hasMore ? 'more' : 'end');
    } catch (e) {
      renderMore('error');
      if (!state.items.length) {
        tbody.innerHTML = '<tr><td colspan="6">' + UI.emptyHTML('加载失败：' + (e.message || ''), '⚠️') + '</td></tr>';
      }
    } finally {
      state.loading = false;
    }
  }

  /* ---------------- 上传 ---------------- */
  $('btnUpload').addEventListener('click', function () {
    UI.modal({
      title: '上传文件',
      body:
        '<div class="form-item"><label class="form-label">文件<span class="required">*</span></label>' +
        '<input class="input" type="file" id="shareFile"></div>' +
        '<div class="form-item"><label class="form-label">文件名称</label>' +
        '<input class="input" id="shareName" maxlength="80" placeholder="留空则使用原文件名"></div>' +
        '<div class="form-item"><label class="form-label">描述</label>' +
        '<input class="input" id="shareDesc" maxlength="100" placeholder="选填"></div>' +
        '<p class="form-tip">内容类型为 multipart/form-data，字段：file / fileName / description</p>',
      okText: '上传',
      onOk: async function (bodyEl) {
        const file = bodyEl.querySelector('#shareFile').files[0];
        if (!file) { UI.toast('请选择文件', 'warning'); return false; }

        const form = new FormData();
        form.append('file', file);
        form.append('fileName', bodyEl.querySelector('#shareName').value.trim() || file.name);
        form.append('description', bodyEl.querySelector('#shareDesc').value.trim());

        await Http.postForm(CONFIG.API.file, form);
        UI.toast('上传成功', 'success');
        await load(true);
      }
    });
  });

  /* ---------------- 下载 / 删除 ---------------- */
  tbody.addEventListener('click', async function (event) {
    const downloadBtn = event.target.closest('[data-download]');
    const delBtn = event.target.closest('[data-del]');

    if (downloadBtn) {
      const id = downloadBtn.getAttribute('data-download');
      try {
        const data = await Http.get(CONFIG.API.file + '/' + id);
        const url = data && data.url;
        if (!url) return UI.toast('下载地址为空', 'warning');
        const a = document.createElement('a');
        a.href = url;
        a.download = (data && data.fileName) || '';
        a.target = '_blank';
        a.click();
      } catch (e) { /* 已 toast */ }
      return;
    }

    if (delBtn) {
      const id = delBtn.getAttribute('data-del');
      const ok = await UI.confirm('确定删除这个文件吗？');
      if (!ok) return;
      await Http.post(CONFIG.API.file + '/rm/' + id);
      UI.toast('删除成功', 'success');
      state.items = state.items.filter(function (f) { return String(f.id) !== String(id); });
      renderList();
    }
  });

  $('btnRefresh').addEventListener('click', function () { load(true); });

  load(true);
})();
