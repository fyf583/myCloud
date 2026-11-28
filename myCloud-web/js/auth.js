/**
 * auth.js —— 登录态管理 + 顶栏渲染 + 模块清单
 * 依赖：config.js → ui.js → request.js → 本文件
 */
(function () {
  const C = window.CONFIG;

  /* ---------------- token / user ---------------- */
  function getToken() { return localStorage.getItem(C.TOKEN_KEY) || ''; }
  function setToken(token) { localStorage.setItem(C.TOKEN_KEY, token); }
  function isLogin() { return !!getToken(); }

  /**
   * 给页面地址加时间戳：浏览器缓存按完整 URL 区分，
   * 这样每次点导航都会重新拉取 HTML（以及其中引用的 CSS），不会再用到旧样式。
   */
  function pageUrl(page) {
    return page + (page.indexOf('?') >= 0 ? '&' : '?') + 't=' + Date.now();
  }

  function getUser() {
    try { return JSON.parse(localStorage.getItem(C.USER_KEY) || 'null'); } catch (e) { return null; }
  }
  function setUser(user) { localStorage.setItem(C.USER_KEY, JSON.stringify(user || {})); }

  /** 只解码不校验签名，仅用于前端展示 */
  function parseJwtPayload(token) {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const binary = decodeURIComponent(
        atob(base64).split('').map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join('')
      );
      return JSON.parse(binary);
    } catch (e) { return null; }
  }

  function currentUserId() {
    const payload = parseJwtPayload(getToken());
    return payload && payload.sub ? Number(payload.sub) : null;
  }

  /* ---------------- 接口 ---------------- */
  /** 登录：后端返回的 data 直接是 token 字符串（兼容 { token: 'xxx' } 写法） */
  async function login(username, password) {
    const data = await Http.post(C.API.auth + '/login', { username: username, password: password }, { auth: false });
    const token = typeof data === 'string' ? data : (data && (data.token || data.accessToken));
    if (!token) throw new Error('登录响应中缺少 token');
    setToken(token);
    setUser({ username: username, userId: currentUserId() });
    return token;
  }

  /** 验证码：后端返回 { captcha, captchaKey }，captcha 是文本验证码 */
  function getCaptcha() {
    return Http.get(C.API.auth + '/captcha', { auth: false });
  }

  /** 注册：注意后端 RegisterDTO 里 captchaKey 是必填项 */
  function register(payload) {
    return Http.post(C.API.auth + '/register', payload, { auth: false });
  }

  async function logout() {
    try {
      await Http.post(C.API.auth + '/logout', undefined, { silent: true });
    } catch (e) { /* 网关已失效也照样退出 */ }
    Http.clearLogin();
    location.href = pageUrl('index.html');
  }

  /** 页面守卫：未登录跳回登录页 */
  function requireLogin() {
    if (!isLogin()) {
      location.replace(pageUrl('index.html'));
      return false;
    }
    return true;
  }

  /* ---------------- 模块清单 ---------------- */
  const MODULES = [
    { key: 'message', name: '留言板', icon: '💬', desc: '公告与留言互动', page: 'message.html', done: true },
    { key: 'note', name: '笔记本', icon: '📒', desc: '笔记内容与分组管理', page: 'note.html', done: true },
    { key: 'diary', name: '日记本', icon: '📔', desc: '记录每天的心情', page: 'diary.html', done: true },
    { key: 'album', name: '相册', icon: '🖼️', desc: '照片上传与浏览', page: 'album.html', done: true },
    { key: 'todo', name: '待做日程', icon: '✅', desc: '待办清单与截止时间', page: 'todo.html', done: true },
    { key: 'file', name: '文件分享', icon: '📁', desc: '文件上传与下载', page: 'file.html', done: true },
    { key: 'song', name: '我的歌单', icon: '🎵', desc: '收藏与播放歌曲', page: 'song.html', done: true }
  ];

  /* ---------------- 顶栏 ---------------- */
  /** 用法：页面里放 <header class="topbar" id="topbar"></header>，然后 Auth.initTopbar('message') */
  function initTopbar(activeKey) {
    const bar = document.getElementById('topbar');
    if (!bar) return;

    const user = getUser() || {};
    const name = user.username || '用户';
    const nav = MODULES.map(function (m) {
      return '<a href="' + pageUrl(m.page) + '"' + (m.key === activeKey ? ' class="active"' : '') + '>' + m.name + '</a>';
    }).join('');

    bar.innerHTML =
      '<div class="topbar-left">' +
        '<a class="brand" href="' + pageUrl('home.html') + '"><span class="logo">云</span>myCloud</a>' +
      '</div>' +
      '<nav class="topbar-nav">' + nav + '</nav>' +
      '<div class="topbar-right">' +
        '<span class="user-chip"><span class="avatar">' + UI.escapeHtml(name.charAt(0)) + '</span>' +
          UI.escapeHtml(name) + '</span>' +
        '<button class="btn btn-ghost btn-sm" id="btnLogout" type="button">退出</button>' +
      '</div>';

    const btn = document.getElementById('btnLogout');
    if (btn) btn.addEventListener('click', function () { logout(); });
  }

  window.Auth = {
    getToken: getToken,
    isLogin: isLogin,
    getUser: getUser,
    currentUserId: currentUserId,
    login: login,
    register: register,
    getCaptcha: getCaptcha,
    logout: logout,
    requireLogin: requireLogin,
    initTopbar: initTopbar,
    pageUrl: pageUrl,
    MODULES: MODULES
  };
})();
