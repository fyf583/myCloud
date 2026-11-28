/**
 * ui.js —— 公共 UI 组件：toast / 确认框 / 通用弹窗 / 时间格式化 / 空状态
 * 依赖：base.css、layout.css
 */
(function () {
  /* ---------------- Toast ---------------- */
  function ensureToastWrap() {
    let wrap = document.querySelector('.toast-wrap');
    if (!wrap) {
      wrap = document.createElement('div');
      wrap.className = 'toast-wrap';
      document.body.appendChild(wrap);
    }
    return wrap;
  }

  function toast(message, type, duration) {
    const wrap = ensureToastWrap();
    const el = document.createElement('div');
    el.className = 'toast' + (type ? ' toast-' + type : '');
    el.textContent = message;
    wrap.appendChild(el);
    setTimeout(function () {
      el.style.transition = 'opacity .2s ease, transform .2s ease';
      el.style.opacity = '0';
      el.style.transform = 'translateY(-8px)';
      setTimeout(function () { el.remove(); }, 220);
    }, duration || 2200);
  }

  /* ---------------- 通用弹窗 ---------------- */
  /**
   * modal({ title, body, okText, cancelText, onOk, danger })
   * body 支持 HTML 字符串或 DOM 元素；onOk 返回 false 时不关闭
   */
  function modal(options) {
    const opts = Object.assign({ title: '提示', body: '', okText: '确定', cancelText: '取消', showCancel: true }, options);

    const mask = document.createElement('div');
    mask.className = 'mask';
    const box = document.createElement('div');
    box.className = 'modal';
    box.innerHTML =
      '<div class="modal-head"><span></span><button class="modal-close" type="button">&times;</button></div>' +
      '<div class="modal-body"></div>' +
      '<div class="modal-foot"></div>';
    box.querySelector('.modal-head span').textContent = opts.title;

    const bodyEl = box.querySelector('.modal-body');
    if (typeof opts.body === 'string') bodyEl.innerHTML = opts.body;
    else if (opts.body) bodyEl.appendChild(opts.body);

    const foot = box.querySelector('.modal-foot');
    const okBtn = document.createElement('button');
    okBtn.className = 'btn ' + (opts.danger ? 'btn-danger' : 'btn-primary');
    okBtn.textContent = opts.okText;
    foot.appendChild(okBtn);

    let cancelBtn = null;
    if (opts.showCancel) {
      cancelBtn = document.createElement('button');
      cancelBtn.className = 'btn btn-ghost';
      cancelBtn.textContent = opts.cancelText;
      foot.insertBefore(cancelBtn, okBtn);
    }

    function close() {
      mask.remove();
      document.removeEventListener('keydown', onKeydown);
    }
    function onKeydown(e) { if (e.key === 'Escape') close(); }

    mask.appendChild(box);
    document.body.appendChild(mask);
    document.addEventListener('keydown', onKeydown);

    box.querySelector('.modal-close').addEventListener('click', close);
    mask.addEventListener('click', function (e) { if (e.target === mask) close(); });
    if (cancelBtn) cancelBtn.addEventListener('click', close);
    okBtn.addEventListener('click', async function () {
      if (typeof opts.onOk === 'function') {
        okBtn.disabled = true;
        try {
          const ret = await opts.onOk(bodyEl, close);
          if (ret !== false) close();
        } finally {
          okBtn.disabled = false;
        }
      } else {
        close();
      }
    });

    return { close: close, body: bodyEl };
  }

  /* ---------------- 确认框 ---------------- */
  function confirmDialog(message, options) {
    const opts = Object.assign({ title: '确认操作', okText: '确定', danger: true }, options);
    return new Promise(function (resolve) {
      const m = modal({
        title: opts.title,
        body: '<div style="color:#4b5563">' + escapeHtml(message) + '</div>',
        okText: opts.okText,
        danger: opts.danger,
        onOk: function () { resolve(true); },
        showCancel: true
      });
      // 关闭（含点取消 / 遮罩 / ESC）统一按 false 处理
      const observer = new MutationObserver(function () {
        if (!document.body.contains(m.body.closest('.mask'))) {
          observer.disconnect();
          resolve(false);
        }
      });
      observer.observe(document.body, { childList: true });
    });
  }

  /* ---------------- 工具函数 ---------------- */
  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  /** 兼容 "2026-11-28T14:23:05" / "2026-11-28T14:23:05.123" / 时间戳 */
  function formatTime(value) {
    if (!value) return '';
    if (typeof value === 'number') {
      const d = new Date(value);
      return pad(d.getFullYear()) + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
        ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
    }
    let s = String(value).replace('T', ' ');
    s = s.split('.')[0];
    return s;
  }
  function pad(n) { return n < 10 ? '0' + n : '' + n; }

  /** 按钮进入 loading 状态，返回恢复函数 */
  function btnLoading(btn, text) {
    if (!btn) return function () {};
    const raw = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span>' + (text || '处理中');
    return function () {
      btn.disabled = false;
      btn.innerHTML = raw;
    };
  }

  function emptyHTML(text, icon) {
    return '<div class="empty"><div class="empty-icon">' + (icon || '🗂️') + '</div>' + escapeHtml(text || '暂无数据') + '</div>';
  }

  window.UI = {
    toast: toast,
    modal: modal,
    confirm: confirmDialog,
    escapeHtml: escapeHtml,
    formatTime: formatTime,
    btnLoading: btnLoading,
    emptyHTML: emptyHTML
  };
})();
