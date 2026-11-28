/**
 * config.js —— 全局配置
 * 说明：所有页面都在最前面引入本文件，其它 js 通过 window.CONFIG 读取。
 */
window.CONFIG = {
  /** 网关地址：前端所有请求都打到这里，由网关转发到各微服务 */
  BASE_URL: 'http://localhost:8080',

  /** 是否启用 mock 总开关 */
  USE_MOCK: true,

  /** 仅这些接口前缀走 mock 假数据（后端尚未实现的模块）
   *  8 个模块后端都已实现，全部走真实接口，所以这里为空 */
  MOCK_PATHS: [],

  /** localStorage 的 key */
  TOKEN_KEY: 'mycloud_token',
  USER_KEY: 'mycloud_user',

  /** 游标分页每页条数（后端上限 20） */
  PAGE_SIZE: 10,

  /** 各模块接口前缀，与 myCloud.md 文档一致 */
  API: {
    auth: '/auth',
    message: '/message',
    note: '/note',
    diary: '/diary',
    album: '/album',
    todo: '/todo',
    file: '/file',
    song: '/song'
  }
};
