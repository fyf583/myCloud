/**
 * pages/login.js —— 登录 / 注册页逻辑
 * 校验规则与后端 DTO 注解保持一致：
 *   RegisterDTO：username @NotBlank、password @NotBlank + @Size(min=6)、password2 @NotBlank、
 *                phone @Pattern(^1[3-9]\d{9}$)、captcha @NotBlank、captchaKey @NotBlank
 *   LoginDTO   ：username @NotBlank、password @NotBlank
 */
(function () {
  const $ = function (id) { return document.getElementById(id); };

  const tabLogin = $('tabLogin');
  const tabRegister = $('tabRegister');
  const loginForm = $('loginForm');
  const registerForm = $('registerForm');
  const loginErr = $('loginErr');
  const registerErr = $('registerErr');
  const captchaBox = $('captchaBox');
  const captchaKeyInput = $('regCaptchaKey');

  /* 已登录直接进首页 */
  if (Auth.isLogin()) {
    location.replace('home.html');
    return;
  }

  /* ---------------- Tab 切换 ---------------- */
  function switchTab(tab) {
    const isLogin = tab === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabRegister.classList.toggle('active', !isLogin);
    loginForm.classList.toggle('hidden', !isLogin);
    registerForm.classList.toggle('hidden', isLogin);
    hideError(loginErr);
    hideError(registerErr);
    if (!isLogin && !captchaKeyInput.value) loadCaptcha();
  }
  tabLogin.addEventListener('click', function () { switchTab('login'); });
  tabRegister.addEventListener('click', function () { switchTab('register'); });
  $('linkToRegister').addEventListener('click', function () { switchTab('register'); });
  $('linkToLogin').addEventListener('click', function () { switchTab('login'); });

  /* ---------------- 错误提示 ---------------- */
  function showError(el, message) {
    el.textContent = message;
    el.classList.remove('hidden');
  }
  function hideError(el) {
    el.textContent = '';
    el.classList.add('hidden');
  }

  /* ---------------- 验证码 ---------------- */
  async function loadCaptcha() {
    captchaBox.classList.add('loading');
    captchaBox.textContent = '加载中…';
    try {
      const data = await Auth.getCaptcha();
      captchaBox.classList.remove('loading');
      captchaBox.textContent = (data && data.captcha) || '刷新';
      captchaKeyInput.value = (data && data.captchaKey) || '';
    } catch (e) {
      captchaBox.classList.remove('loading');
      captchaBox.textContent = '点击重试';
      captchaKeyInput.value = '';
    }
  }
  captchaBox.addEventListener('click', loadCaptcha);

  /* ---------------- 登录 ---------------- */
  loginForm.addEventListener('submit', async function (event) {
    event.preventDefault();
    hideError(loginErr);

    const username = $('loginUsername').value.trim();
    const password = $('loginPassword').value;
    if (!username) return showError(loginErr, '请输入账号');
    if (!password) return showError(loginErr, '请输入密码');

    const btn = $('btnLogin');
    const restore = UI.btnLoading(btn, '登录中');
    try {
      await Auth.login(username, password);
      UI.toast('登录成功', 'success');
      setTimeout(function () { location.href = 'home.html'; }, 300);
    } catch (e) {
      showError(loginErr, e.message || '登录失败');
    } finally {
      restore();
    }
  });

  /* ---------------- 注册 ---------------- */
  registerForm.addEventListener('submit', async function (event) {
    event.preventDefault();
    hideError(registerErr);

    const username = $('regUsername').value.trim();
    const password = $('regPassword').value;
    const password2 = $('regPassword2').value;
    const phone = $('regPhone').value.trim();
    const captcha = $('regCaptcha').value.trim();
    const captchaKey = captchaKeyInput.value;

    if (!username) return showError(registerErr, '请输入账号');
    if (!password) return showError(registerErr, '请输入密码');
    if (password.length < 6) return showError(registerErr, '密码至少 6 位');
    if (password !== password2) return showError(registerErr, '两次输入的密码不一致');
    if (!phone) return showError(registerErr, '请输入手机号');
    if (!/^1[3-9]\d{9}$/.test(phone)) return showError(registerErr, '手机号格式不正确');
    if (!captcha) return showError(registerErr, '请输入验证码');
    if (!captchaKey) return showError(registerErr, '验证码已失效，请点击方块刷新');

    const btn = $('btnRegister');
    const restore = UI.btnLoading(btn, '注册中');
    try {
      await Auth.register({
        username: username,
        password: password,
        password2: password2,
        phone: phone,
        captcha: captcha,
        captchaKey: captchaKey
      });
      UI.toast('注册成功，请登录', 'success');
      registerForm.reset();
      captchaKeyInput.value = '';
      captchaBox.textContent = '点击获取';
      switchTab('login');
      $('loginUsername').value = username;
      $('loginPassword').focus();
    } catch (e) {
      showError(registerErr, e.message || '注册失败');
      loadCaptcha();
    } finally {
      restore();
    }
  });

  $('loginUsername').focus();
})();
