# World of Zuul — 小组协同开发项目

基于经典文本冒险游戏 **World of Zuul** 扩展 8 项功能，采用 Maven 构建、Checkstyle 规范检查、JUnit 测试与 GitHub Actions CI/CD。

**仓库地址：** https://github.com/wutcst/kai-fa-519club

## 小组成员

| 姓名 | GitHub | 分工 |
|------|--------|------|
| （填写） | @xxx | 组长 / 集成 |
| （填写） | @xxx | 功能开发 |
| （填写） | @xxx | 功能开发 / 测试 |

## 功能列表

- [ ] F1：房间内任意数量物品 + `look` 展示
- [ ] F2：单步 `back` 回退
- [ ] F3：多步历史 `back`
- [ ] F4：随机传送房间
- [ ] F5：`Player` / `take` / `drop` / `items` / `eat cookie`
- [ ] F6：网络多人模式（简化版）
- [ ] F7：图形界面
- [ ] F8：数据库持久化（H2）

## 分支模型

| 分支 | 说明 |
|------|------|
| `master` | 稳定发布版，**仅**接受来自 `dev` 的 PR |
| `dev` | 开发集成分支，合并所有已验收功能 |
| `feature/issue号-简述` | 个人功能开发分支 |
| `fix/issue号-简述` | 缺陷修复分支 |

## 开发流程（组员必读）

1. 在 GitHub 领取 Issue，确认验收标准。
2. 从 `dev` 拉取最新代码，创建 `feature/12-xxx` 分支。
3. 本地通过门禁后再推送：
   ```powershell
   mvn checkstyle:check
   mvn test
   ```
4. 推送后发起 PR → `dev`，等待 **CI Pipeline** 全部通过。
5. 组长/同伴 Review 后合并；其他人 `git pull origin dev` 同步。

详细约定见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 技术栈

- Java 11
- Maven 3.8+
- JUnit 5
- Checkstyle
- GitHub Actions
- H2 Database（功能 8 预留）

## 构建与运行

```powershell
mvn clean package
java -jar target/world-of-zuul-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

仅编译与测试：

```powershell
mvn clean validate
mvn test
```

## CI / DevOps

推送至 `dev` 或对 `master`/`dev` 发起 PR 时，自动执行：

1. Checkstyle 代码规范检查  
2. JUnit 单元测试  
3. Maven 打包并上传可执行 JAR（Actions → Artifacts → `game-jar`）

## 提交信息规范

```
feat: 新功能简述 (#12)
fix: 修复问题 (#8)
test: 补充测试
docs: 文档
chore: 构建或配置
```

## 交付物

- [ ] 根目录 `REPORT.pdf` 或 `REPORT.docx`（实训报告）
- [ ] B 站演示视频（标题前缀：**【武理26软工实践】**）
- [ ] 答辩用 `master` 稳定版本

## 参考：实训任务说明

本项目对应《软件工程实训任务二：小组协同开发》，涵盖 Issue 分工、分支协作、自动化检查与打包发布等要求。
