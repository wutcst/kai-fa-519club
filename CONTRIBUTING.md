# 贡献指南（小组协同开发规范）

本项目：《熄灯前归寝》— World of Zuul 扩展（五关解密 + Spring Boot REST + **Vue 3 图形前端** + H2）。

## 1. 分支模型（精简，禁止滥开分支）

全仓库**只保留两类长期分支 + 四类辅助分支**，不为每个小功能单独开 `feature/xxx`。

### 长期分支（禁止直接乱提交）

| 分支 | 说明 |
|------|------|
| `master` | 答辩发布版，**仅组长**从 `dev` 提 PR 合并 |
| `dev` | 集成分支，所有验收通过的代码合并到此 |

### 辅助分支（全组最多四类）

| 类型 | 分支名 | 谁用 | 说明 |
|------|--------|------|------|
| **个人开发** | `feature/xmq` `feature/lj` `feature/phx` `feature/pqj` | 每人固定一个 | 日常写代码、修 Bug；**不另开** `feature/gui` 等第四分支 |
| **文档** | `feature/docs` | 全员可提交 | 仅 `docs/`、`README.md`、`CONTRIBUTING.md` 等文档 |
| **调试** | `debug` | 需实验时 | 临时调试，**不得**合并进 `master`；验证完合并到 `dev` 或丢弃 |
| **冲突解决** | `merge/conflict` | 发生冲突者 | 仅在 `dev` 合并冲突时创建，解决完毕立即 PR 合并并**删除** |

**禁止：** 一人多个个人分支、随意新建 `feature/gui` 等功能向分支（仅允许上表四个 `feature/姓名拼音`；历史多余分支合并后应删除）。

### 个人分支对照

| 成员 | 个人开发分支 |
|------|----------------|
| 肖梦琪 | `feature/xmq` |
| 刘晶 | `feature/lj` |
| 彭慧星 | `feature/phx` |
| 庞绮君 | `feature/pqj` |

## 2. 开始工作前

**写代码（在个人分支）：**

```powershell
git checkout dev
git pull origin dev
git checkout feature/xmq
git merge dev
```

若本地尚无个人分支：

```powershell
git checkout -b feature/xmq
git push -u origin feature/xmq
```

**写文档（在 feature/docs 分支）：**

```powershell
git checkout dev
git pull origin dev
git checkout feature/docs
git merge dev
```

**解决合并冲突：**

```powershell
git checkout dev
git pull origin dev
git checkout -b merge/conflict
# 合并各 feature/xxx 或手动解决冲突后提交
```

## 3. 提交前本地门禁（必须）

```powershell
mvn checkstyle:check
mvn test
mvn package -DskipTests=false
```

若改动 `vue-portal/`（Vue 前端），另执行：

```powershell
cd vue-portal
npm run build
```

任一步失败则**不要**推送。

## 4. 提交信息格式

每次提交须写清**做了什么**（组员看 `git log` 能懂）。

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

**示例：**

```
feat: 实现距熄灯23:00秒数倒计时
fix: 西楼合成锤子后出口仍锁定
docs: 更新会议记录与分工
```

## 5. Pull Request

- 个人开发：`feature/xmq`（或 lj/phx/pqj）→ **`dev`**
- 文档：`feature/docs` → **`dev`**
- 冲突解决：`merge/conflict` → **`dev`**（合并后删分支）
- 发布：**仅组长** `dev` → `master`
- PR 描述须说明：改了什么、本地测了什么、是否影响熄灯计时 / 存档 / 联机
- 填写 [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)

## 6. 代码冲突与解决

1. 开发前先 `git pull origin dev` 并 `merge` 进个人分支，减少冲突。  
2. 出现冲突时，在 `merge/conflict` 分支解决，**禁止**强行 `push --force` 到 `dev` / `master`。  
3. 解决后重新 `mvn test`，PR 中注明冲突文件与验证结果。  
4. 合并完成后删除 `merge/conflict`：`git branch -d merge/conflict`。

## 7. 代码规范要点（与 `checkstyle.xml` 一致）

- 包名：`cn.edu.whut.sept.zuul`
- 缩进：4 空格，禁止 Tab
- 每行不超过 120 字符
- 禁止 `import xxx.*` 星号导入
- `if/for/while` 必须使用大括号 `{ }`
- 计时文案统一：**距熄灯（23:00）还有 XXX 秒**

## 8. 面向对象要求

- 新命令实现 `CommandInterface`，在 `CommandManager` 中注册（包路径 `command/`）
- 新实体独立成类，避免在 `Game` 中堆逻辑
- 单元测试类名以 `Test` 结尾

## 9. 合并后同步

```powershell
git checkout dev
git pull origin dev
git checkout feature/xmq
git merge dev
```

## 10. 分工与功能编号

| 成员 | 个人分支 | 主责 |
|------|----------|------|
| 肖梦琪 | `feature/xmq` | 架构 E9—E14、**F6 服务端**、**F8/H2/认证**、Spring API 桥接 |
| 刘晶 | `feature/lj` | 核心命令 E1—E4/E8 等、**F7 Vue GUI**（经典/沉浸）、联机前端协作 |
| 彭慧星 | `feature/phx` | E5、E6 `sleep`、E17、第 3—4 关与 E16 |
| 庞绮君 | `feature/pqj` | E15、E7、E14、第 1—2 关与 E16（5 关）、文档与测试 |

功能清单见 [README.md](README.md)、[docs/会议记录.md](docs/会议记录.md)。
