/**
 * pages/diary.js —— 日记本
 * 接口：GET/POST /diary、GET/POST /diary/{diaryId}、POST /diary/rm/{diaryId}
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('diary');

  const $ = function (id) { return document.getElementById(id); };
  const listEl = $('diaryList');
  const moreWrap = $('loadMoreWrap');

  const state = { items: [], cursorId: null, cursorTime: null, hasMore: true, loading: false };

  function renderList() {
    if (!state.items.length) {
      listEl.innerHTML = UI.emptyHTML(state.loading ? '加载中…' : '还没有日记，写下第一篇吧', '📔');
      return;
    }
    listEl.innerHTML = state.items.map(function (d) {
      return '' +
        '<div class="timeline-item">' +
          '<div class="timeline-date">' + UI.escapeHtml(UI.formatTime(d.createTime)) +
            '<span class="text-sub" style="margin-left:8px">#' + d.id + '</span>' +
            '<button class="btn-text btn-sm" data-edit="' + d.id + '" type="button">编辑</button>' +
            '<button class="btn-text danger btn-sm" data-del="' + d.id + '" type="button">删除</button>' +
          '</div>' +
          '<div class="timeline-text">' + UI.escapeHtml(d.diary) + '</div>' +
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
      const data = await Http.get(CONFIG.API.diary, { query: payload });
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

  /* ---------------- 写 / 改 ---------------- */
  function openEditor(diary) {
    const isEdit = !!diary;
    UI.modal({
      title: isEdit ? '编辑日记' : '写日记',
      body: '<div class="form-item"><label class="form-label">今天发生了什么？<span class="required">*</span></label>' +
            '<textarea class="textarea" id="diaryText" rows="8" maxlength="3000" placeholder="记录一下…">' +
            (diary ? UI.escapeHtml(diary.diary) : '') + '</textarea></div>',
      okText: '保存',
      onOk: async function (bodyEl) {
        const text = bodyEl.querySelector('#diaryText').value.trim();
        if (!text) { UI.toast('日记内容不能为空', 'warning'); return false; }
        if (isEdit) {
          await Http.post(CONFIG.API.diary + '/' + diary.id, { id: diary.id, diary: text });
        } else {
          await Http.post(CONFIG.API.diary, { diary: text });
        }
        UI.toast('保存成功', 'success');
        await load(true);
      }
    });
  }

  $('btnNewDiary').addEventListener('click', function () { openEditor(null); });

  listEl.addEventListener('click', async function (event) {
    const editBtn = event.target.closest('[data-edit]');
    const delBtn = event.target.closest('[data-del]');

    if (editBtn) {
      const diary = state.items.find(function (d) { return String(d.id) === editBtn.getAttribute('data-edit'); });
      if (diary) openEditor(diary);
      return;
    }
    if (delBtn) {
      const id = delBtn.getAttribute('data-del');
      const ok = await UI.confirm('确定删除这篇日记吗？');
      if (!ok) return;
      await Http.post(CONFIG.API.diary + '/rm/' + id);
      UI.toast('删除成功', 'success');
      state.items = state.items.filter(function (d) { return String(d.id) !== String(id); });
      renderList();
    }
  });

  $('btnRefresh').addEventListener('click', function () { load(true); });

  load(true);
})();
