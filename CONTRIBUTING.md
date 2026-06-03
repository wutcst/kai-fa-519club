# 贡献指南（小组协同开发规范）

## 1. 开始工作前

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/12-player-take
```

分支命名：`feature/<issue号>-<英文简述>` 或 `fix/<issue号>-<简述>`。

## 2. 提交前本地门禁（必须）

```powershell
mvn checkstyle:check
mvn test
mvn package -DskipTests=false
```

任一步失败则**不要**推送。

## 3. 提交信息格式

```
<type>: <简述> (#issue号)
```

| type | 含义 |
|------|------|
| feat | 新功能 |
| fix | 修复缺陷 |
| test | 测试 |
| docs | 文档 |
| refactor | 重构 |
| chore | 构建/配置 |

示例：`feat: 实现 take 命令 (#12)`

## 4. Pull Request

- 目标分支：`dev`（功能）或 `dev` → `master`（发布，仅组长发起）
- 标题与 commit 风格一致
- 描述中写 `Closes #12` 关联 Issue
- 填写 PR 模板中的测试与自查项

## 5. 代码规范要点（与 `checkstyle.xml` 一致）

- 包名：`cn.edu.whut.sept.zuul`
- 缩进：4 空格，禁止 Tab；类/方法左大括号可单独一行（BlueJ 风格）
- 每行不超过 120 字符
- 禁止 `import xxx.*` 星号导入；`java` → `javax` → `org` → `com` → `cn` 分组导入
- `if/for/while` 必须使用大括号 `{ }`
- 文本游戏可使用 `System.out`；新增 public API 建议补充 Javadoc

## 6. 面向对象要求

- 新命令继承 `Command`，在 `CommandWords` 注册
- 新游戏实体（物品、玩家等）独立成类，避免在 `Game` 中堆逻辑
- 单元测试放在 `src/test/java`，类名以 `Test` 结尾

## 7. 合并后同步

```powershell
git checkout dev
git pull origin dev
git branch -d feature/12-player-take
```
