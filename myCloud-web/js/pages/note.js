/**
 * pages/note.js —— 笔记本
 * 接口：GET/POST /note、GET/POST /note/{noteId}、POST /note/group、
 *       POST /note/{noteId}/group?typeId=xxx、POST /note/rm/{noteId}
 * 说明：后端未实现时，由 js/mock.js 提供数据（CONFIG.USE_MOCK = true）。
 */
(function () {
  if (!Auth.requireLogin()) return;
  Auth.initTopbar('note');

  const $ = function (id) { return document.getElementById(id); };
  const listEl = $('noteList');
  const moreWrap = $('loadMoreWrap');

  const state = {
    notes: [],
    groups: [],
    cursorId: null,
    cursorTime: null,
    hasMore: true,
    loading: false,
    groupId: null
  };

  /* ---------------- 分组 ---------------- */
  async function loadGroups() {
    try {
      const data = await Http.get(CONFIG.API.note + '/group', { query: { pageSize: 100 }, silent: true });
      state.groups = Http.normalizePage(data).list || [];
    } catch (e) {
      state.groups = [];
    }
    renderGroups();
  }

  function groupName(typeId) {
    const group = state.groups.find(function (g) { return g.id === typeId; });
    return group ? group.groupName : '';
  }

  function renderGroups() {
    const items = ['<div class="side-item' + (state.groupId === null ? ' active' : '') + '" data-gid="">全部笔记</div>'];
    state.groups.forEach(function (g) {
      items.push('<div class="side-item' + (state.groupId === g.id ? ' active' : '') + '" data-gid="' + g.id + '">' +
        UI.escapeHtml(g.groupName) + '</div>');
    });
    $('groupList').innerHTML = items.join('');
  }

  $('groupList').addEventListener('click', function (event) {
    const item = event.target.closest('[data-gid]');
    if (!item) return;
    const raw = item.getAttribute('data-gid');
    state.groupId = raw ? Number(raw) : null;
    $('noteTitle').textContent = state.groupId === null ? '全部笔记' : ('分组：' + (groupName(state.groupId) || '未知'));
    renderGroups();
    renderNotes();
  });

  $('btnNewGroup').addEventListener('click', function () {
    UI.modal({
      title: '新建分组',
      body: '<div class="form-item"><label class="form-label">分组名称<span class="required">*</span></label>' +
            '<input class="input" id="newGroupName" maxlength="20" placeholder="例如：学习笔记"></div>',
      okText: '创建',
      onOk: async function (bodyEl) {
        const name = bodyEl.querySelector('#newGroupName').value.trim();
        if (!name) { UI.toast('请输入分组名称', 'warning'); return false; }
        await Http.post(CONFIG.API.note + '/group', { groupName: name });
        UI.toast('分组创建成功', 'success');
        await loadGroups();
      }
    });
  });

  /* ---------------- 笔记列表 ---------------- */
  function renderNotes() {
    const list = state.groupId === null
      ? state.notes
      : state.notes.filter(function (n) { return n.typeId === state.groupId; });

    if (!list.length) {
      listEl.innerHTML = UI.emptyHTML('这里还没有笔记', '📒');
      return;
    }

    listEl.innerHTML = list.map(function (n) {
      return '' +
        '<div class="list-item">' +
          '<div class="list-item-main">' +
            '<div class="list-item-title" style="white-space:pre-wrap">' + UI.escapeHtml(n.note) + '</div>' +
            '<div style="display:flex;gap:2px;flex-shrink:0">' +
              '<button class="btn-text btn-sm" data-edit="' + n.id + '" type="button">编辑</button>' +
              '<button class="btn-text btn-sm" data-move="' + n.id + '" type="button">分组</button>' +
              '<button class="btn-text danger btn-sm" data-del="' + n.id + '" type="button">删除</button>' +
            '</div>' +
          '</div>' +
          '<div class="list-item-meta">' +
            '<span class="meta-tag">' + UI.escapeHtml(groupName(n.typeId) || '未分组') + '</span>#' + n.id +
            ' · ' + UI.escapeHtml(UI.formatTime(n.createTime)) +
          '</div>' +
        '</div>';
    }).join('');
  }

  function renderMore(mode) {
    if (mode === 'loading') {
      moreWrap.innerHTML = '<div class="loading">加载中</div>';
    } else if (mode === 'more') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnMore" type="button">加载更多</button>';
      $('btnMore').addEventListener('click', function () { loadNotes(false); });
    } else if (mode === 'end') {
      moreWrap.innerHTML = state.notes.length ? '<div class="no-more">— 没有更多了 —</div>' : '';
    } else if (mode === 'error') {
      moreWrap.innerHTML = '<button class="btn btn-ghost btn-sm" id="btnRetry" type="button">加载失败，点击重试</button>';
      $('btnRetry').addEventListener('click', function () { loadNotes(true); });
    }
  }

  async function loadNotes(reset) {
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
      const data = await Http.get(CONFIG.API.note, { query: payload });
      const page = Http.normalizePage(data);
      state.notes = reset ? page.list : state.notes.concat(page.list);
      state.hasMore = page.hasMore;
      state.cursorId = page.cursorId;
      state.cursorTime = page.cursorTime;
      renderNotes();
      renderMore(state.hasMore ? 'more' : 'end');
    } catch (e) {
      renderMore('error');
      if (!state.notes.length) listEl.innerHTML = UI.emptyHTML('加载失败：' + (e.message || ''), '⚠️');
    } finally {
      state.loading = false;
    }
  }

  async function reload() {
    await Promise.all([loadGroups(), loadNotes(true)]);
  }

  /* ---------------- 新建 / 编辑 ---------------- */
  function openEditor(note) {
    const isEdit = !!note;
    const options = ['<option value="">未分组</option>'].concat(
      state.groups.map(function (g) {
        const selected = note && note.typeId === g.id ? ' selected' : '';
        return '<option value="' + g.id + '"' + selected + '>' + UI.escapeHtml(g.groupName) + '</option>';
      })
    ).join('');

    UI.modal({
      title: isEdit ? '编辑笔记' : '新建笔记',
      body:
        '<div class="form-item"><label class="form-label">笔记内容<span class="required">*</span></label>' +
        '<textarea class="textarea" id="editorNote" rows="6" maxlength="2000" placeholder="写点什么…">' +
        (note ? UI.escapeHtml(note.note) : '') + '</textarea></div>' +
        (isEdit
          ? '<div class="form-item"><label class="form-label">所属分组</label>' +
            '<select class="select" id="editorGroup">' + options + '</select></div>'
          : '<p class="form-tip">新建后可用列表里的「分组」按钮归类</p>'),
      okText: '保存',
      onOk: async function (bodyEl) {
        const text = bodyEl.querySelector('#editorNote').value.trim();
        if (!text) { UI.toast('笔记内容不能为空', 'warning'); return false; }

        if (isEdit) {
          await Http.post(CONFIG.API.note + '/' + note.id, { id: note.id, note: text });
          const select = bodyEl.querySelector('#editorGroup');
          const typeId = select ? select.value : '';
          if (String(typeId || '') !== String(note.typeId || '')) {
            await Http.post(CONFIG.API.note + '/' + note.id + '/group', undefined, {
              query: { typeId: typeId }
            });
          }
        } else {
          await Http.post(CONFIG.API.note, { note: text });
        }
        UI.toast('保存成功', 'success');
        await reload();
      }
    });
  }

  $('btnNewNote').addEventListener('click', function () { openEditor(null); });

  /* ---------------- 列表操作 ---------------- */
  listEl.addEventListener('click', async function (event) {
    const editBtn = event.target.closest('[data-edit]');
    const moveBtn = event.target.closest('[data-move]');
    const delBtn = event.target.closest('[data-del]');

    if (editBtn) {
      const note = state.notes.find(function (n) { return String(n.id) === editBtn.getAttribute('data-edit'); });
      if (note) openEditor(note);
      return;
    }

    if (moveBtn) {
      const note = state.notes.find(function (n) { return String(n.id) === moveBtn.getAttribute('data-move'); });
      if (!note) return;
      const options = ['<option value="">未分组</option>'].concat(
        state.groups.map(function (g) {
          const selected = note.typeId === g.id ? ' selected' : '';
          return '<option value="' + g.id + '"' + selected + '>' + UI.escapeHtml(g.groupName) + '</option>';
        })
      ).join('');
      UI.modal({
        title: '移动到分组',
        body: '<div class="form-item"><label class="form-label">目标分组</label>' +
              '<select class="select" id="moveGroup">' + options + '</select></div>',
        okText: '确定',
        onOk: async function (bodyEl) {
          const typeId = bodyEl.querySelector('#moveGroup').value;
          await Http.post(CONFIG.API.note + '/' + note.id + '/group', undefined, { query: { typeId: typeId } });
          UI.toast('已移动', 'success');
          await reload();
        }
      });
      return;
    }

    if (delBtn) {
      const id = delBtn.getAttribute('data-del');
      const ok = await UI.confirm('确定删除这条笔记吗？');
      if (!ok) return;
      await Http.post(CONFIG.API.note + '/rm/' + id);
      UI.toast('删除成功', 'success');
      state.notes = state.notes.filter(function (n) { return String(n.id) !== String(id); });
      renderNotes();
    }
  });

  $('btnRefresh').addEventListener('click', reload);

  /* ---------------- 初始化 ---------------- */
  reload();
})();
