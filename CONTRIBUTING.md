# 贡献指南（小组协同开发规范）

本项目：《熄灯前归寝》— World of Zuul 扩展（五关解密 + 多人 + GUI + H2）。

## 1. 开始工作前

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/level-manager
```

### 分支命名

| 类型 | 格式 | 示例 |
|------|------|------|
| 功能开发 | `feature/xxx` | `feature/gui`、`feature/multiplayer`、`feature/level-3-4` |
| 缺陷修复 | `fix/xxx` | `fix/timer-display`、`fix/west-building-door` |

说明：

- `xxx` 为**简短表意**英文或拼音，见名知意即可。
- **不强制** Issue 编号，也不使用 `feature/12-xxx` 旧格式。
- 一人可同时开多个 `feature/*` 分支，合并后及时删除本地分支。

## 2. 提交前本地门禁（必须）

```powershell
mvn checkstyle:check
mvn test
mvn package -DskipTests=false
```

任一步失败则**不要**推送。

## 3. 提交信息格式

每次提交须写清**做了什么、为什么**（组员阅读 `git log` 能懂）。

```
<type>: <简述>
```

| type | 含义 |
|------|------|
| feat | 新功能 |
| fix | 修复缺陷 |
| test | 测试 |
| docs | 文档 |
| refactor | 重构 |
| chore | 构建/配置 |

**示例（推荐中文简述）：**

```
feat: 实现距熄灯23:00秒数倒计时
feat: 第五关体育馆随机传送
feat: H2 保存当前关卡与剩余秒数
feat: JavaFX 状态栏显示背包与倒计时
fix: 西楼合成锤子后出口仍锁定
docs: 更新会议记录与分工
```

可选在简述末尾注明 Issue：`feat: 图形界面主窗口 (#15)`，**非必须**。

## 4. Pull Request

- 目标分支：`dev`（日常开发）；`dev` → `master`（发布，**仅组长肖梦琪**发起）
- 标题与 commit 风格一致，例如：`feat: 简化版网络多人联机`
- PR 描述须包含：
  - 改动文件与原因（段落即可）
  - 本地测试结果（checkstyle / test / 手动场景）
  - 是否影响「距熄灯（23:00）」计时、存档或多人同步
- 填写 [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)

## 5. 代码规范要点（与 `checkstyle.xml` 一致）

- 包名：`cn.edu.whut.sept.zuul`
- 缩进：4 空格，禁止 Tab；类/方法左大括号可单独一行（BlueJ 风格）
- 每行不超过 120 字符
- 禁止 `import xxx.*` 星号导入；`java` → `javax` → `org` → `com` → `cn` 分组导入
- `if/for/while` 必须使用大括号 `{ }`
- 文本模式可使用 `System.out`；GUI 与 public API 建议补充 Javadoc

## 6. 面向对象要求

- 新命令继承 `Command`，在 `CommandWords` 注册
- 新游戏实体（物品、玩家、关卡配置等）独立成类，避免在 `Game` 中堆逻辑
- 单元测试放在 `src/test/java`，类名以 `Test` 结尾
- 计时统一走 `LevelTimer`，界面文案使用：**距熄灯（23:00）还有 XXX 秒**

## 7. 合并后同步

```powershell
git checkout dev
git pull origin dev
git branch -d feature/level-manager
```

## 8. 功能编号与建议分支

**已实现（维护）：** F1—F5（`look`、单步/多步 `back`、传送房、`Player` 与 `take`/`drop`/`items`/`eat cookie`）。

**待开发拓展：** E1—E17（含 `use`、`submit`、`combine`、`unlock`、`feed`、`sleep`、五关与熄灯计时等）。

**待开发高级：** F6 多人、F7 GUI、F8 H2。

| 模块 | 建议分支 | 主要负责人 | 权重 |
|------|----------|------------|------|
| 关卡框架、计时、房间规则 E9—E13/E15、H2 核心、F7 对接 | `feature/level-manager`、`feature/room-rules`、`feature/h2-core` | 肖梦琪 | 32% |
| 拓展命令 E1—E8、第 1—3 关、干扰物、命令测试 | `feature/use-submit`、`feature/level-1-2-3` | 刘晶 | 30% |
| 第 4 关、E12—E14 联调、E17 传送、F6 服务端 | `feature/level-4`、`feature/multiplayer-server` | 彭慧星 | 19% |
| 第 5 关数据、F7 界面、F8 DAO、文档 | `feature/level-5`、`feature/gui`、`feature/h2-dao` | 庞绮君 | 19% |

完整列表见 [README.md](README.md) 与 [docs/会议记录.md](docs/会议记录.md)。
