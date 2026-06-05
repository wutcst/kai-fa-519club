# 《熄灯前归寝》— World of Zuul 小组协同开发项目

基于经典文本冒险游戏 **World of Zuul** 扩展，主题为午夜从校门回寝室睡觉。采用 Maven 构建、Checkstyle 规范检查、JUnit 测试与 GitHub Actions CI/CD。

**仓库地址：** https://github.com/wutcst/kai-fa-519club  
**项目周期：** 2026 年 6 月 1 日 — 6 月 20 日  
**熄灯时刻：** 23:00（界面显示 **距熄灯（23:00）还有 XXX 秒**）

## 小组成员

| 姓名 | 角色 | 权重 | 主要负责 |
|------|------|------|----------|
| 肖梦琪 | 组长 | **约 32%** | 架构集成、`LevelManager` / `LevelTimer`、房间规则底层、H2/F7 对接层、第 4 关规则、发布 |
| 刘晶 | 组员 | **约 30%** | 拓展命令 E1—E8（`use` / `submit` 等）、第 1—3 关配置、干扰物与命令测试 |
| 彭慧星 | 组员 | **约 19%** | 第 4 关、西楼/黑暗联调、F6 服务端、第五关传送 |
| 庞绮君 | 组员 | **约 19%** | 第 5 关数据、F7 界面、F8 DAO、文档与部分测试 |

> 肖梦琪、刘晶承担约 **62%** 核心开发量；详见 [docs/会议记录.md](docs/会议记录.md) 第三节。

---

## 功能总览

功能分三类：**实训基础五项（已完成）**、**本作拓展功能（待开发）**、**实训高级三项（待开发）**。完整说明见 [docs/会议记录.md](docs/会议记录.md)。

### 一、实训基础功能 F1—F5（✅ 已实现）

对应课程要求前五项，当前代码库已具备：

| 编号 | 功能 | 说明 |
|------|------|------|
| F1 | 房间物品 + `look` | 房间可放置任意数量物品（含描述与重量）；`look` 显示房间与物品信息 |
| F2 | 单步 `back` | 返回上一房间 |
| F3 | 多步 `back` | 可连续回退至多步，直至起点 |
| F4 | 随机传送房间 | `TeleportRoom`，进入后随机传送到其他房间 |
| F5 | `Player` 与携带物 | 玩家姓名、当前房间、背包与负重上限；`take` / `drop` / `items`；魔法饼干 + `eat cookie` 增加负重 |

### 二、拓展功能 E1—E17（⬜ 待实现，本作必做）

在 F1—F5 之上，为《熄灯前归寝》五关玩法新增的命令、关卡与规则（**`use`、`submit` 等均属此类**）：

| 编号 | 功能 | 说明 |
|------|------|------|
| E1 | `use` | 使用物品：超市 `use` 登记条/三十元换一卡通、西楼 `use` 锤子砸锁、超市 `use` 夜归单等 |
| E2 | `submit` | 寝室门口提交物品：提交归寝单 / 退寝条；错误物品直接失败 |
| E3 | `combine` | 合成物品：棍子 + 石头 + 绳子 → 锤子 |
| E4 | `unlock` | 密码解锁：体育馆值班室、第五关寝室八位密码 `20000527` |
| E5 | `feed` | 喂猫学长：消耗火腿肠，耗时 1 分钟，获得魔法饼干 |
| E6 | `sleep` | 关卡通关动作：在寝室执行，满足当关条件后过关 |
| E7 | `eat`（饼干加时） | 食用魔法饼干，距熄灯 **+300 秒**（与 F5 负重饼干区分用途或共用物品双效果） |
| E8 | NPC 对话 | 宿管阿姨换卡、志愿者 / 图书馆领归寝单；`talk` 或 `register` |
| E9 | `LevelManager` | 五关进度、通关解锁下一关、失败重开本关 |
| E10 | `LevelTimer` | 距熄灯 **（23:00）还有 XXX 秒**；归零失败 |
| E11 | 操作耗时 | `go` / `look` / `take` / `drop` / `use` 等按策划扣减秒数 |
| E12 | 黑暗区域罚时 | 博学主楼断电：无手电筒进入罚 1 分钟并退回 |
| E13 | 条件门 | 图书馆、寝室须持一卡通；部分关卡须额外物品或 `submit` |
| E14 | 西楼困锁 | 进西楼被困，须合成锤子后出门 |
| E15 | 关卡房间解锁 | 按关开放地图子集（第 1 关 5 房 → 第 5 关全图） |
| E16 | 干扰物品与公告 | 各房间进入公告、诱惑型干扰物描述与拾取 |
| E17 | 第五关传送干扰 | 体育馆随机传送（复用 F4 `TeleportRoom`，按关启用） |

### 三、实训高级功能 F6—F8（⬜ 待实现，必做）

| 编号 | 功能 | 说明 |
|------|------|------|
| F6 | 网络多人模式 | 简化联机：登录、服务端世界状态、命令同步、熄灯倒计时广播 |
| F7 | 图形界面 | JavaFX 或 Swing：房间、背包、命令、关卡、**距熄灯秒数**、日志 |
| F8 | 数据库 H2 | 存档：当前关卡、剩余秒数、通关记录、可选多人昵称 |

---

## 开发进度勾选（组内维护）

**基础（已完成）**

- [x] F1 房间物品 + `look`
- [x] F2 单步 `back`
- [x] F3 多步 `back`
- [x] F4 随机传送房间
- [x] F5 `Player` / `take` / `drop` / `items` / `eat cookie`

**拓展（待完成）**

- [ ] E1 `use`
- [ ] E2 `submit`
- [ ] E3 `combine`
- [ ] E4 `unlock`
- [ ] E5 `feed`
- [ ] E6 `sleep`
- [ ] E7 魔法饼干加时
- [ ] E8 NPC 对话
- [ ] E9 `LevelManager`
- [ ] E10 `LevelTimer`（距 23:00 秒数）
- [ ] E11 操作耗时
- [ ] E12 黑暗罚时
- [ ] E13 条件门
- [ ] E14 西楼困锁
- [ ] E15 关卡房间解锁
- [ ] E16 干扰物与公告
- [ ] E17 第五关体育馆传送

**高级（待完成）**

- [ ] F6 网络多人
- [ ] F7 图形界面
- [ ] F8 数据库 H2

---

## 分支模型

| 分支 | 说明 |
|------|------|
| `master` | 稳定发布版，**仅**接受来自 `dev` 的 PR（组长发起） |
| `dev` | 开发集成分支，合并所有已验收功能 |
| `feature/xxx` | 个人功能分支，`xxx` 为简短描述，**不强制 Issue 号** |
| `fix/xxx` | 缺陷修复分支 |

示例：`feature/use-submit`、`feature/level-manager`、`feature/gui`、`feature/multiplayer`、`feature/h2-save`

## 开发流程（组员必读）

1. 确认分工与验收标准（见 [docs/会议记录.md](docs/会议记录.md)）。
2. 从 `dev` 拉取最新代码，创建分支，例如：
   ```powershell
   git checkout dev
   git pull origin dev
   git checkout -b feature/use-submit
   ```
3. 本地通过门禁后再推送：
   ```powershell
   mvn checkstyle:check
   mvn test
   ```
4. 提交时写清描述，例如：`feat: 实现 submit 归寝单与寝室通关判定`。
5. 推送后发起 PR → `dev`，等待 **CI Pipeline** 全部通过。
6. 组长 Review 后合并；其他人 `git pull origin dev` 同步。

详细约定见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 技术栈

- Java 11
- Maven 3.8+
- JUnit 5
- Checkstyle
- GitHub Actions
- H2 Database（F8）
- JavaFX 或 Swing（F7）
- Java Socket（F6）

## 构建与运行

```powershell
mvn clean package
java -jar target/world-of-zuul-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## 交付物（6 月 20 日前）

- [ ] E1—E17 拓展功能全部可玩（五关流程贯通）
- [ ] F6 多人可演示
- [ ] F7 图形界面可演示
- [ ] F8 H2 存档与读档
- [ ] 根目录 `REPORT.pdf` 或 `REPORT.docx`
- [ ] B 站演示视频（标题前缀：**【武理26软工实践】**）
- [ ] 答辩用 `master` 稳定版本

## 参考文档

- [软件工程实践说明（架构 / DevOps / 分支 / 评审 / CI）](docs/软件工程实践说明.md)
- [会议记录与分工时间表](docs/会议记录.md)
- [贡献指南与分支规范](CONTRIBUTING.md)
