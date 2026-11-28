/**
 * mock.js —— 未实现模块（笔记/日记/相册/待做/文件/歌单）的假数据源
 *
 * 工作方式：request.js 在 CONFIG.USE_MOCK === true 时，先把 (method, path, data, query)
 * 交给 Mock.match()；返回 undefined 表示「不拦截，走真实请求」。
 * 后端这些模块上线后，把 js/config.js 里的 USE_MOCK 改成 false 即可整体切换到真实接口。
 */
(function () {
  /* ============ 模拟数据 ============ */
  const DB = {
    noteGroup: [
      { id: 1, groupName: '学习笔记', status: 1, createTime: '2026-11-01T09:00:00', updateTime: null },
      { id: 2, groupName: '工作记录', status: 1, createTime: '2026-11-03T09:00:00', updateTime: null },
      { id: 3, groupName: '生活随笔', status: 1, createTime: '2026-11-05T09:00:00', updateTime: null }
    ],
    note: [
      { id: 5, note: 'Spring Cloud Gateway 的 GlobalFilter 执行顺序由 Ordered 决定，值越小越先执行。', typeId: 1, status: 1, createTime: '2026-11-26T20:10:00', updateTime: null },
      { id: 4, note: 'MyBatis 的 <trim> 标签可以自动处理多余的逗号，常配合 <if> 做动态 SQL。', typeId: 1, status: 1, createTime: '2026-11-25T15:32:00', updateTime: null },
      { id: 3, note: '周五前完成留言模块的自测：分页、删除、异常提示。', typeId: 2, status: 1, createTime: '2026-11-24T10:05:00', updateTime: null },
      { id: 2, note: '用 ThreadLocal + Filter 把网关注入的用户信息传递到 Service 层。', typeId: 1, status: 1, createTime: '2026-11-22T21:40:00', updateTime: null },
      { id: 1, note: '今天把项目结构重新梳理了一遍，明确了后续 6 个模块的排期。', typeId: 3, status: 1, createTime: '2026-11-20T19:00:00', updateTime: null }
    ],
    diary: [
      { id: 4, diary: '把网关的跨域和预检请求处理好了，前端页面终于能跑通登录。', status: 1, createTime: '2026-11-27T22:30:00', updateTime: null },
      { id: 3, diary: '研究了一下游标分页，比 offset 分页在数据量大时更稳。', status: 1, createTime: '2026-11-26T23:10:00', updateTime: null },
      { id: 2, diary: '今天效率一般，只写了留言模块的查询接口。', status: 1, createTime: '2026-11-25T21:50:00', updateTime: null },
      { id: 1, diary: '开坑 myCloud，希望这个项目能撑起简历。', status: 1, createTime: '2026-11-20T22:00:00', updateTime: null }
    ],
    todo: [
      { id: 4, content: '补全笔记模块的 CRUD 接口', deadline: '2026-12-05T18:00:00', status: 0, createTime: '2026-11-27T09:00:00', updateTime: null },
      { id: 3, content: '把留言模块的 SQL 自测一遍', deadline: '2026-11-29T18:00:00', status: 0, createTime: '2026-11-26T09:00:00', updateTime: null },
      { id: 2, content: '整理 myCloud.md 接口文档', deadline: '2026-11-28T18:00:00', status: 1, createTime: '2026-11-24T09:00:00', updateTime: '2026-11-28T10:00:00' },
      { id: 1, content: '搭建 Nacos + 网关骨架', deadline: '2026-11-22T18:00:00', status: 1, createTime: '2026-11-20T09:00:00', updateTime: '2026-11-21T16:00:00' }
    ],
    album: [
      { id: 3, photoName: '海边的日落.jpg', description: '十月旅行', url: 'https://picsum.photos/id/1015/800/600', status: 1, createTime: '2026-11-24T18:20:00' },
      { id: 2, photoName: '雪山.jpg', description: '徒步记录', url: 'https://picsum.photos/id/1018/800/600', status: 1, createTime: '2026-11-22T12:10:00' },
      { id: 1, photoName: '城市夜景.jpg', description: '随手拍', url: 'https://picsum.photos/id/1039/800/600', status: 1, createTime: '2026-11-20T21:05:00' }
    ],
    file: [
      { id: 3, fileName: 'myCloud-接口文档.pdf', description: 'v1.0 接口说明', size: 245760, url: '#', status: 1, createTime: '2026-11-27T11:00:00' },
      { id: 2, fileName: 'spring-cloud-notes.zip', description: '学习资料', size: 5242880, url: '#', status: 1, createTime: '2026-11-25T09:30:00' },
      { id: 1, fileName: '简历.pdf', description: '求职用', size: 102400, url: '#', status: 1, createTime: '2026-11-20T08:00:00' }
    ],
    song: [
      { id: 3, songName: '晴天', singer: '周杰伦', url: 'https://example.com/song/qingtian.mp3', status: 1, createTime: '2026-11-26T20:00:00' },
      { id: 2, songName: '海底', singer: '一支榴莲', url: 'https://example.com/song/haidi.mp3', status: 1, createTime: '2026-11-23T20:00:00' },
      { id: 1, songName: '起风了', singer: '买辣椒也用券', url: 'https://example.com/song/qifengle.mp3', status: 1, createTime: '2026-11-20T20:00:00' }
    ]
  };

  /* ============ 工具 ============ */
  function nowStr() {
    const d = new Date();
    function p(n) { return n < 10 ? '0' + n : '' + n; }
    return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) +
      'T' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
  }

  function nextId(list) {
    return list.reduce(function (max, item) { return Math.max(max, item.id); }, 0) + 1;
  }

  /** 模拟后端的游标分页：按 createTime 倒序，返回 hasMore / nextCursorId / nextCursorTime / list */
  function paginate(list, payload) {
    const pageSize = (payload && payload.pageSize) || 10;
    const sorted = list.slice().sort(function (a, b) {
      return String(b.createTime).localeCompare(String(a.createTime));
    });
    let start = 0;
    if (payload && payload.cursorId) {
      const idx = sorted.findIndex(function (item) { return item.id === payload.cursorId; });
      start = idx >= 0 ? idx + 1 : 0;
    }
    const slice = sorted.slice(start, start + pageSize);
    const last = slice[slice.length - 1];
    return {
      hasMore: start + pageSize < sorted.length,
      nextCursorId: last ? last.id : ((payload && payload.cursorId) || null),
      nextCursorTime: last ? last.createTime : ((payload && payload.cursorTime) || null),
      list: slice
    };
  }

  /** FormData 与普通对象的统一取值 */
  function readValue(payload, key) {
    if (typeof FormData !== 'undefined' && payload instanceof FormData) {
      const v = payload.get(key);
      return v && typeof v === 'object' ? v.name : v;
    }
    return payload ? payload[key] : undefined;
  }

  function build(mod, payload) {
    const now = nowStr();
    if (mod === 'note') {
      return { id: nextId(DB.note), note: readValue(payload, 'note') || '', typeId: Number(readValue(payload, 'typeId')) || null, status: 1, createTime: now, updateTime: now };
    }
    if (mod === 'diary') {
      return { id: nextId(DB.diary), diary: readValue(payload, 'diary') || '', status: 1, createTime: now, updateTime: now };
    }
    if (mod === 'todo') {
      return { id: nextId(DB.todo), content: readValue(payload, 'content') || '', deadline: readValue(payload, 'deadline') || null, status: 0, createTime: now, updateTime: now };
    }
    if (mod === 'album') {
      return {
        id: nextId(DB.album),
        photoName: readValue(payload, 'photoName') || readValue(payload, 'file') || '新照片',
        description: readValue(payload, 'description') || '',
        url: 'https://picsum.photos/seed/' + Date.now() + '/800/600',
        status: 1,
        createTime: now
      };
    }
    if (mod === 'file') {
      return {
        id: nextId(DB.file),
        fileName: readValue(payload, 'fileName') || readValue(payload, 'file') || '新文件',
        description: readValue(payload, 'description') || '',
        size: 102400,
        url: '#',
        status: 1,
        createTime: now
      };
    }
    return { id: nextId(DB.song), songName: readValue(payload, 'songName') || '新歌曲', singer: readValue(payload, 'singer') || '', url: readValue(payload, 'url') || '#', status: 1, createTime: now };
  }

  /* ============ 路由：模拟各模块接口 ============ */
  function match(method, path, data, query) {
    const hit = /^\/(note|diary|todo|album|file|song)(\/.*)?$/.exec(path);
    if (!hit) return undefined; // 不是这些模块 → 放行走真实请求

    const mod = hit[1];
    const segs = (hit[2] || '').split('/').filter(Boolean);
    const list = DB[mod];

    /* GET /xxx —— 列表 */
    if (method === 'GET' && segs.length === 0) return paginate(list, data || query);

    /* POST /xxx —— 新建 */
    if (method === 'POST' && segs.length === 0) {
      list.unshift(build(mod, data));
      return {};
    }

    if (segs.length === 1) {
      /* POST /note/group —— 新建分组 */
      if (mod === 'note' && method === 'POST' && segs[0] === 'group') {
        const group = { id: nextId(DB.noteGroup), groupName: (data && data.groupName) || '新分组', status: 1, createTime: nowStr(), updateTime: null };
        DB.noteGroup.unshift(group);
        return { id: group.id };
      }
      /* GET /note/group —— 分组列表（文档未定义，仅 mock 内部使用） */
      if (mod === 'note' && method === 'GET' && segs[0] === 'group') return paginate(DB.noteGroup, data || query);

      const id = Number(segs[0]);
      /* GET /xxx/{id} —— 详情 */
      if (method === 'GET') {
        return list.find(function (item) { return item.id === id; }) || null;
      }
      /* POST /xxx/{id} —— 修改 */
      if (method === 'POST') {
        const item = list.find(function (i) { return i.id === id; });
        if (item) {
          Object.keys(data || {}).forEach(function (key) { if (key !== 'id') item[key] = data[key]; });
          item.updateTime = nowStr();
        }
        return {};
      }
    }

    if (segs.length === 2) {
      /* POST /note/{noteId}/group?typeId=xxx —— 笔记分组（id 在路径第一段） */
      if (mod === 'note' && method === 'POST' && segs[1] === 'group') {
        const noteId = Number(segs[0]);
        const item = list.find(function (i) { return i.id === noteId; });
        if (item) {
          item.typeId = Number((query && query.typeId) || (data && data.typeId)) || null;
          item.updateTime = nowStr();
        }
        return {};
      }

      const id = Number(segs[1]);
      /* POST /xxx/rm/{id} —— 删除 */
      if (method === 'POST' && segs[0] === 'rm') {
        const idx = list.findIndex(function (i) { return i.id === id; });
        if (idx >= 0) list.splice(idx, 1);
        return {};
      }
      /* POST /todo/done/{id} —— 完成日程 */
      if (method === 'POST' && segs[0] === 'done') {
        const item = list.find(function (i) { return i.id === id; });
        if (item) { item.status = 1; item.updateTime = nowStr(); }
        return {};
      }
    }

    return undefined;
  }

  window.Mock = { match: match, DB: DB };
})();
