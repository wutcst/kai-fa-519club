\# World of Zuul - 小组协同开发项目



<!-- 项目简介 -->

\## 项目简介

基于经典游戏 World of Zuul，扩展 8 项功能。本项目遵循严格的工业级开发规范，包含 CI/CD、代码规范检查、单元测试等。



<!-- 功能列表（待完成） -->

\## 功能列表

\- \[ ] 功能1：房间内显示任意数量物品

\- \[ ] 功能2：单步 back 命令

\- \[ ] 功能3：多步历史回退

\- \[ ] 功能4：随机传输房间

\- \[ ] 功能5：玩家背包、重量限制、take/drop/items/eat cookie

\- \[ ] 功能6：网络多人模式（简化版）

\- \[ ] 功能7：图形化界面

\- \[ ] 功能8：数据库持久化



<!-- 分支模型说明 -->

\## 分支模型

\- `main`：稳定发布版，仅接受来自 `dev` 的 PR

\- `dev`：开发主分支，所有功能分支合并至此

\- `feature/\*`：个人功能分支

\- `fix/\*`：bug 修复分支



<!-- 技术栈 -->

\## 技术栈

\- Java 11

\- Maven 3.8+

\- JUnit 5

\- Checkstyle

\- GitHub Actions (CI)

\- H2 Database



<!-- 如何构建和运行 -->

\## 构建与运行

```bash

mvn clean package

java -jar target/world-of-zuul-1.0.0-SNAPSHOT-jar-with-dependencies.jar

