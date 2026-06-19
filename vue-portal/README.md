# Vue 游戏前端（vue-portal）

《熄灯前归寝》唯一图形客户端：**Vue 3 MVC 分层**，支持 **经典版 / 沉浸版** 双显示模式（复用 `/assets/gui` 图片）。

- **单机 L1→L5**：`/solo`（Spring `/api/solo` 驱动）
- **多人联机**：`/multiplayer`（Spring `/api/rooms`、`/api/game`）

## MVC 架构

```
src/
  model/          # 数据与领域（types、soloTypes、sessionModel、assetCatalog、roomLayoutDefaults、gameDisplayMode）
  service/        # REST 访问（httpClient、soloService、roomService、gameService）
  controller/     # 业务编排（useSoloGameController、useLobbyController、useGameRoomController）
  view/           # 页面壳（HomeView、SoloGameView、LobbyView、MultiplayerGameView）
  component/      # 可复用 UI（solo/*、game/*、lobby/*、common/*）
  styles/         # 全局主题 token
  router/
```

| 层 | 职责 |
|----|------|
| **Model** | 类型定义、会话状态、物品锚点、显示模式、`/assets/gui` 路径映射 |
| **Service** | 调用 Spring REST，不含 UI 逻辑 |
| **Controller** | Composable 编排：轮询、发令、进房/离房、单人命令 |
| **View** | 只组合组件，不写业务 |
| **Component** | 玻璃 HUD、全屏场景、方向导航、背包槽、结局/锁门/合成弹层 |

## 显示模式

| 模式 | 说明 |
|------|------|
| **经典版** | 平面 HUD：方向栏、背包、命令栏 |
| **沉浸版** | 全屏场景 + 暗角转场 + Q 版背影 + 计时压力 + 音效 |

首页与局内均可切换，偏好保存在 `localStorage`（键 `519club.displayMode`）。

## 前置条件

1. 启动 Spring Boot 服务端（默认 `8080`）：

```powershell
cd D:\softwaretest\kai-fa-519club
mvn spring-boot:run
```

或使用脚本：

```powershell
.\scripts\run-multiplayer-server.ps1
```

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
| 首页 `/` | 选择单人或联机；经典/沉浸切换；输入昵称后创建单人会话 |
| 单人 `/solo` | L1→L5；移动、拾取、吃/用/丢、环顾、对话、结局/锁门/合成 |
| 联机大厅 `/multiplayer` | 校门背景 + 玻璃面板；房间列表、创建/加入 |
| 联机对局 `/multiplayer/room` | 与单机共用场景 UI；轮询、移动、环顾、命令、聊天 |

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
