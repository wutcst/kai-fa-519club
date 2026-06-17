# Vue 游戏前端（vue-portal）

《熄灯前归寝》Web 客户端：**Vue 3 MVC 分层** + 沉浸式游戏 UI（复用 `/assets/gui` 图片）。

- **单机 L1→L5**：本目录 `/solo`（Spring `/api/solo` 驱动，与 Enhanced Swing 玩法一致）
- **多人联机**：本目录 `/multiplayer`（Spring `/api/rooms`、`/api/game`）
- **Swing Enhanced**：仍可作为备用单机入口；推荐优先使用 Vue 单机

## MVC 架构

```
src/
  model/          # 数据与领域（types、soloTypes、sessionModel、soloSessionModel、assetCatalog、theme）
  service/        # REST 访问（httpClient、soloService、roomService、gameService）
  controller/     # 业务编排（useSoloGameController、useLobbyController、useGameRoomController）
  view/           # 页面壳（HomeView、SoloGameView、LobbyView、MultiplayerGameView）
  component/      # 可复用 UI（solo/*、game/*、lobby/*、common/*）
  styles/         # 全局主题（对齐 Swing GuiTheme）
  router/
```

| 层 | 职责 |
|----|------|
| **Model** | 类型定义、会话状态、`/assets/gui` 路径映射 |
| **Service** | 调用 Spring REST，不含 UI 逻辑 |
| **Controller** | Composable 编排：轮询、发令、进房/离房、单人命令 |
| **View** | 只组合组件，不写业务 |
| **Component** | 玻璃 HUD、全屏场景、方向导航、背包槽、结局/锁门/合成弹层 |

## 沉浸式 UI（对齐 Enhanced Swing）

- 全屏房间底图 + 暗角 + 轻微 Ken Burns 动画
- 换房间时 fade + 遮罩过渡
- 右上计时 HUD（≤120s 橙、≤60s 红闪）
- 中央方向箭头 + 环顾/返回
- 左下 6 格背包（物品图标走 `assetCatalog`）
- 左上同房间玩家列表
- 底部命令栏 + 玻璃公告弹层

## 前置条件

1. 启动 Spring Boot 联机服务端（默认 `8080`）：

**PowerShell（推荐，项目已配置主类，无需 `-D`）：**

```powershell
cd D:\softwaretest\kai-fa-519club
mvn spring-boot:run
```

或使用脚本：

```powershell
.\scripts\run-multiplayer-server.ps1
```

若必须指定主类，请给 `-D` 加引号（否则 PowerShell 会拆参数）：

```powershell
mvn spring-boot:run "-Dspring-boot.run.mainClass=cn.edu.whut.sept.zuul.infrastructure.server.ServerApplication"
```

或在 IDEA 中直接运行 `ServerApplication`。

2. 安装 Node.js 18+，在本目录安装依赖：

```bash
cd vue-portal
npm install
npm run dev
```

浏览器打开 [http://localhost:5173](http://localhost:5173)。Vite 会将 `/api` 与 `/assets` 代理到 `8080`。

## 功能

| 页面 | 能力 |
|------|------|
| 首页 `/` | 选择单人或联机；输入昵称后创建单人会话 |
| 单人 `/solo` | L1→L5 沉浸式 HUD；移动、拾取、吃/用/丢、环顾、对话、结局/锁门/合成 |
| 联机大厅 `/multiplayer` | 校门背景 + 玻璃面板；房间列表、创建/加入 |
| 联机对局 `/multiplayer/room` | 全屏沉浸式 HUD；轮询、移动、环顾、命令、背包图标 |

## REST 对接

**单人 `/api/solo`**

- `POST /sessions` — 创建会话
- `GET /sessions/{id}/state` — 拉取视图状态
- `POST /command` — 执行命令（移动、拾取等）
- `POST /look`、`POST /talk` — 环顾、对话
- `POST /outcome/dismiss`、`POST /locked/dismiss` — 关闭结局/锁门弹层
- `DELETE /sessions/{id}` — 结束会话

**联机**

- `GET /api/rooms` — 房间列表
- `POST /api/rooms` — 创建房间
- `POST /api/rooms/{id}/join` — 加入
- `POST /api/rooms/{id}/leave` — 离开
- `GET /api/game/state` — 轮询状态
- `POST /api/game/command` — 执行命令

会话信息保存在 `sessionStorage`（联机键 `zuul-portal-session`，单人键 `zuul-solo-session`）。

## 生产构建

```bash
npm run build
npm run preview
```

构建产物在 `dist/`。部署时需反向代理 `/api` 与 `/assets` 到 Java 服务端。

## 与 Swing 的关系

Swing `enhanced` 仍为早期单机实现；**推荐 L1→L5 使用 Vue `/solo`**。联机推荐 Vue `/multiplayer`；Swing 联机壳（`MultiplayerLobbyDialog`）为遗留入口。
