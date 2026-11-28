/**
 * request.js —— 统一请求封装
 *
 * 设计要点：
 *  1. 所有请求打到网关 CONFIG.BASE_URL，由网关转发到各微服务；
 *  2. 自动带上 token 请求头（注意：后端网关 AuthGlobalFilter 读取的 header 名就叫 "token"，
 *     不是 Authorization）；
 *  3. 统一解析后端响应 { code, msg, data }，并兼容文档里的 { code, message, data }；
 *  4. 分页结果兼容 { nextCursorId, nextCursorTime, hasMore, list } 与
 *     { cursorId, cursorTime, hasMore, dataList } 两种口径；
 *  5. HTTP 401 时清空登录态并跳回登录页；
 *  6. CONFIG.USE_MOCK 为 true 时，交给 js/mock.js 拦截未实现模块的请求。
 */
(function () {
  const C = window.CONFIG;

  function getToken() {
    return localStorage.getItem(C.TOKEN_KEY) || '';
  }

  function clearLogin() {
    localStorage.removeItem(C.TOKEN_KEY);
    localStorage.removeItem(C.USER_KEY);
  }

  function buildQuery(query) {
    if (!query) return '';
    const parts = [];
    Object.keys(query).forEach(function (key) {
      const value = query[key];
      if (value === undefined || value === null || value === '') return;
      parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
    });
    return parts.length ? '?' + parts.join('&') : '';
  }

  /** 判断该接口是否走 mock（仅 CONFIG.MOCK_PATHS 中列出的模块） */
  function isMockPath(path) {
    if (!C.USE_MOCK) return false;
    const paths = C.MOCK_PATHS || [];
    return paths.some(function (prefix) {
      return path === prefix || path.indexOf(prefix + '/') === 0;
    });
  }

  function redirectToLogin() {
    clearLogin();
    if (!/index\.html?$/i.test(location.pathname)) {
      setTimeout(function () { location.href = 'index.html'; }, 600);
    }
  }

  async function request(path, options) {
    const opts = Object.assign(
      { method: 'GET', data: undefined, query: undefined, isForm: false, auth: true, silent: false },
      options || {}
    );

    // 兜底：浏览器规范禁止 GET / HEAD 携带请求体（fetch 会直接抛 TypeError），
    // 这里把误传的 data 自动降级为 query 参数，避免请求静默失败
    if ((opts.method === 'GET' || opts.method === 'HEAD') && opts.data !== undefined && !opts.isForm) {
      opts.query = Object.assign({}, opts.data, opts.query || {});
      opts.data = undefined;
    }

    // ---- mock 拦截（仅 USE_MOCK 打开且命中 MOCK_PATHS 时生效，见 js/mock.js） ----
    if (isMockPath(path) && window.Mock && typeof window.Mock.match === 'function') {
      const mocked = window.Mock.match(opts.method, path, opts.data, opts.query);
      if (mocked !== undefined) {
        return mocked;
      }
    }

    const url = C.BASE_URL + path + buildQuery(opts.query);
    const headers = {};
    if (opts.auth) {
      const token = getToken();
      if (token) headers['token'] = token;
    }

    let body;
    if (opts.data !== undefined) {
      if (opts.isForm) {
        body = opts.data; // FormData：让浏览器自动带 boundary
      } else {
        headers['Content-Type'] = 'application/json';
        body = JSON.stringify(opts.data);
      }
    }

    let res;
    try {
      res = await fetch(url, { method: opts.method, headers: headers, body: body });
    } catch (e) {
      if (!opts.silent) UI.toast('网络异常：请确认网关（' + C.BASE_URL + '）已启动', 'error');
      throw new Error('NETWORK_ERROR');
    }

    if (res.status === 401) {
      if (!opts.silent) UI.toast('登录已过期，请重新登录', 'warning');
      redirectToLogin();
      throw new Error('UNAUTHORIZED');
    }

    let json = null;
    try {
      json = await res.json();
    } catch (e) {
      json = null;
    }

    if (!json) {
      if (!opts.silent) UI.toast('接口不可用（HTTP ' + res.status + '），该模块可能尚未实现', 'error');
      throw new Error('BAD_RESPONSE');
    }

    const code = json.code;
    const message = json.msg || json.message || '操作失败';
    if (code !== 200) {
      if (!opts.silent) UI.toast(message, 'error');
      throw new Error(message);
    }
    return json.data;
  }

  /** 统一分页结构，屏蔽后端/文档字段差异 */
  function normalizePage(data) {
    const d = data || {};
    const list = d.list || d.dataList || [];
    return {
      list: list,
      hasMore: !!d.hasMore,
      cursorId: d.nextCursorId !== undefined && d.nextCursorId !== null ? d.nextCursorId : (d.cursorId || null),
      cursorTime: d.nextCursorTime !== undefined && d.nextCursorTime !== null ? d.nextCursorTime : (d.cursorTime || null)
    };
  }

  window.Http = {
    request: request,
    get: function (path, opt) { return request(path, Object.assign({ method: 'GET' }, opt || {})); },
    post: function (path, data, opt) { return request(path, Object.assign({ method: 'POST', data: data }, opt || {})); },
    postForm: function (path, formData, opt) {
      return request(path, Object.assign({ method: 'POST', data: formData, isForm: true }, opt || {}));
    },
    normalizePage: normalizePage,
    getToken: getToken,
    clearLogin: clearLogin
  };
})();
