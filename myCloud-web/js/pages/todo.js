/**
 * pages/todo.js —— 待做日程
 * 接口：GET/POST /todo、POST /todo/{todoId}、POST /todo/done/{todoId}、POST /todo/rm/{todoId}
 * status：0 未完成、1 已完成、删除态由后端标记
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('todo');

  const $ = function (id) { return document.getElementById(id); };
  const listEl = $('todoList');
  const moreWrap = $('loadMoreWrap');

  const state = { items: [], cursorId: null, cursorTime: null, hasMore: true, loading: false, status: '' };

  function statusText(status) {
    return Number(status) === 1 ? '已完成' : '未完成';
  }

  function renderList() {
    const list = state.status === ''
      ? state.items
      : state.items.filter(function (t) { return String(t.status) === state.status; });

    if (!list.length) {
      listEl.innerHTML = UI.emptyHTML(state.loading ? '加载中…' : '暂无日程，添加一条吧', '✅');
      return;
    }

    listEl.innerHTML = list.map(function (t) {
      const done = Number(t.status) === 1;
      return '' +
        '<div class="list-item">' +
          '<div class="list-item-main">' +
            '<div class="list-item-title' + (done ? ' todo-done' : '') + '">' + UI.escapeHtml(t.content) + '</div>' +
            '<div style="display:flex;gap:2px;flex-shrink:0">' +
              (done ? '' : '<button class="btn-text btn-sm" data-done="' + t.id + '" type="button">完成</button>') +
              '<button class="btn-text btn-sm" data-edit="' + t.id + '" type="button">编辑</button>' +
              '<button class="btn-text danger btn-sm" data-del="' + t.id + '" type="button">删除</button>' +
            '</div>' +
          '</div>' +
          '<div class="list-item-meta">' +
            '<span class="meta-tag">' + statusText(t.status) + '</span>' +
            (t.deadline ? '截止 ' + UI.escapeHtml(UI.formatTime(t.deadline)) + ' · ' : '') +
            '#' + t.id + ' · ' + UI.escapeHtml(UI.formatTime(t.createTime)) +
          '</div>' +
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
      const data = await Http.get(CONFIG.API.todo, { query: payload });
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

  /* ---------------- 新建 / 编辑 ---------------- */
  function openEditor(todo) {
    const isEdit = !!todo;
    const deadline = todo && todo.deadline ? String(todo.deadline).replace(' ', 'T').slice(0, 16) : '';
    UI.modal({
      title: isEdit ? '编辑日程' : '新建日程',
      body:
        '<div class="form-item"><label class="form-label">待做内容<span class="required">*</span></label>' +
        '<input class="input" id="todoContent" maxlength="100" placeholder="例如：补全笔记模块接口" value="' +
        (todo ? UI.escapeHtml(todo.content) : '') + '"></div>' +
        '<div class="form-item"><label class="form-label">截止时间</label>' +
        '<input class="input" type="datetime-local" id="todoDeadline" value="' + UI.escapeHtml(deadline) + '"></div>',
      okText: '保存',
      onOk: async function (bodyEl) {
        const content = bodyEl.querySelector('#todoContent').value.trim();
        const deadlineValue = bodyEl.querySelector('#todoDeadline').value;
        if (!content) { UI.toast('待做内容不能为空', 'warning'); return false; }

        const payload = { content: content, deadline: deadlineValue || null };
        if (isEdit) {
          payload.id = todo.id;
          payload.status = todo.status;
          await Http.post(CONFIG.API.todo + '/' + todo.id, payload);
        } else {
          await Http.post(CONFIG.API.todo, payload);
        }
        UI.toast('保存成功', 'success');
        await load(true);
      }
    });
  }

  $('btnNewTodo').addEventListener('click', function () { openEditor(null); });

  /* ---------------- 列表操作 ---------------- */
  listEl.addEventListener('click', async function (event) {
    const doneBtn = event.target.closest('[data-done]');
    const editBtn = event.target.closest('[data-edit]');
    const delBtn = event.target.closest('[data-del]');

    if (doneBtn) {
      const id = doneBtn.getAttribute('data-done');
      await Http.post(CONFIG.API.todo + '/done/' + id, { id: Number(id), status: 1 });
      UI.toast('已标记完成', 'success');
      await load(true);
      return;
    }

    if (editBtn) {
      const todo = state.items.find(function (t) { return String(t.id) === editBtn.getAttribute('data-edit'); });
      if (todo) openEditor(todo);
      return;
    }

    if (delBtn) {
      const id = delBtn.getAttribute('data-del');
      const ok = await UI.confirm('确定删除这条日程吗？');
      if (!ok) return;
      await Http.post(CONFIG.API.todo + '/rm/' + id, { id: Number(id) });
      UI.toast('删除成功', 'success');
      state.items = state.items.filter(function (t) { return String(t.id) !== String(id); });
      renderList();
    }
  });

  /* ---------------- 过滤 / 刷新 ---------------- */
  $('statusFilter').addEventListener('change', function () {
    state.status = this.value;
    renderList();
  });

  $('btnRefresh').addEventListener('click', function () { load(true); });

  load(true);
})();
