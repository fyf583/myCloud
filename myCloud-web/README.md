# myCloud-web

myCloud 个人云笔记系统的前端页面（原生 HTML + CSS + JS，无需构建工具、无 npm 依赖）。

## 目录结构

```
myCloud-web/
├─ index.html        登录 / 注册
├─ home.html         首页：模块入口
├─ message.html      留言板（后端已实现，可真跑通）
├─ note.html         笔记本（后端未实现，展示示例数据）
├─ diary.html        日记本（同上）
├─ album.html        相册（同上）
├─ todo.html         待做日程（同上）
├─ file.html         文件分享（同上）
├─ song.html         我的歌单（同上）
├─ css/
│  ├─ base.css       变量 / 重置 / 排版
│  └─ layout.css     顶栏 / 卡片 / 表单 / 表格 / 弹窗 / 分页
└─ js/
   ├─ config.js      全局配置（网关地址、mock 开关、接口前缀）
   ├─ request.js     统一 fetch 封装（token 头、响应解析、字段兼容、401 跳转）
   ├─ auth.js        登录态 + 顶栏渲染 + 模块清单
   ├─ ui.js          toast / confirm / modal / 时间格式化
   ├─ mock.js        未实现模块的假数据
   └─ pages/         各页面业务逻辑
```

## 启动方式

### 1. 启动后端（顺序不能乱）

1. Nacos：`localhost:8848`
2. `myCloud-user-service`（8081）
3. `myCloud-message-service`（8082）
4. `myCloud-gateway-service`（8080）← 前端所有请求都打到这里

### 2. 启动前端

用 VS Code 的 **Live Server** 插件打开 `index.html`（默认地址 `http://127.0.0.1:5500/index.html`），
或任意静态服务器，例如：

```bash
npx http-server . -p 5500
```

> 不要用 `file://` 直接双击打开：浏览器的同源策略会让所有请求失败（Origin 为 null）。

### 3. 跨域

网关 `myCloud-gateway-service/src/main/resources/application.yaml` 已配置 `spring.cloud.gateway.globalcors`，
允许 `http://localhost:5500`、`http://127.0.0.1:5500` 等来源访问。
如果你的 Live Server 端口不同，请在该配置里追加对应 origin。

## 配置项（js/config.js）

| 配置 | 说明 |
|---|---|
| `BASE_URL` | 网关地址，默认 `http://localhost:8080` |
| `USE_MOCK` | `true` 时未实现模块走 `js/mock.js` 假数据；后端上线后改为 `false` |
| `PAGE_SIZE` | 游标分页每页条数，默认 10（后端上限 20） |

## 接口口径说明（前端已做兼容）

| 场景 | 后端实际 | 文档写法 | 前端处理 |
|---|---|---|---|
| 统一响应 | `{ code, msg, data }` | `{ code, message, data }` | 取 `msg \|\| message` |
| 分页结果 | `{ nextCursorId, nextCursorTime, hasMore, list }` | `{ cursorId, cursorTime, hasMore, dataList }` | `Http.normalizePage()` 统一 |
| 登录参数 | `username` / `password` | `userName` / `password` | 按后端 `username` |
| 登录响应 | `data` 直接是 token 字符串 | `data.token` | 两种都兼容 |
| 注册参数 | 比文档多一个必填 `captchaKey` | 文档缺失 | 先调 `/auth/captcha` 获取 |
| 鉴权头 | `token`（不是 `Authorization`） | 文档未写 | `request.js` 统一注入 |

## 已知问题 / 待优化

1. `GET /message`、`GET /message/{userId}` 在后端使用 `@RequestBody` 接收分页参数，
   前端以「GET + body」方式调用。浏览器允许，但部分代理会丢弃 GET 的 body，
   建议后端改为 `@ModelAttribute CursorPage`（Query 参数）。改完后把
   `js/pages/message.js` 里 `{ data: payload }` 换成 `{ query: payload }` 即可。
2. 3~8 章（笔记 / 日记 / 相册 / 待做 / 文件 / 歌单）后端尚未实现，页面当前展示 mock 数据。
