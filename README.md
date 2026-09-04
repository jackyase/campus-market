# 校园二手集市（Campus Market）

一个前后端分离的校园二手交易平台，覆盖「发布 → 搜索 → 加购 → 下单 → 支付 → 发货 → 收货 → 评价」完整买卖闭环，并附带社区与商家中心。

## 技术栈

- **后端**：Spring Boot 4.1.1 · Java 17 · Spring Data JPA · Spring Security + JWT · MySQL 8.4
- **前端**：Vue 3 · Vite · Element Plus · Pinia · Vue Router · Axios

## 功能

- 注册登录（JWT 无状态认证）
- 商品：分类浏览、上架、图片上传、搜索筛选、详情
- 交易：购物车、下单、模拟支付、订单流转（待付款 → 已付款 → 已发货 → 已收货）
- 评价：商品 / 卖家双评分
- 个人主页：封面、头像、自我介绍、兴趣标签、关注
- 社区：发帖（图 / 视频）、评论、点赞、关注、话题
- 商家中心：开店、货架上下架、发货、店铺统计

## 本地运行

### 前置要求

- JDK 17
- MySQL 8.4（默认端口 `3307`，账号 `root` / `123456`，库名 `campus_market`）
- Node.js 18+

### 后端

```bash
./gradlew bootRun
```

启动前请确保 MySQL 已运行，并执行建库脚本：

```bash
mysql -u root -p < sql/init.sql
```

数据表由 JPA（`ddl-auto: update`）自动创建，分类种子数据在启动时自动初始化。

接口文档（Swagger UI）：启动后访问 http://localhost:8080/swagger-ui.html

### 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173（已配置代理转发到后端 8080）。

## 配置

后端配置见 `src/main/resources/application.yml`。所有敏感项均支持**环境变量覆盖**，未设置时使用本地开发默认值：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 服务端口 |
| `DB_URL` | `jdbc:mysql://127.0.0.1:3307/campus_market?...` | 数据库连接 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | 内置开发密钥 | JWT 签名密钥 |
| `JWT_EXPIRATION` | `86400000` | Token 有效期（毫秒） |
| `UPLOAD_DIR` | `D:/campus-market/upload` | 文件上传目录（本地磁盘，预留 MinIO / OSS 切换） |

生产或他人环境请通过环境变量注入真实值，勿将真实密码提交进仓库。
