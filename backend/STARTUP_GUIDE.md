# 后端项目启动手册

## 项目概述

**项目名称**：酒店价格监控系统后端服务  
**技术栈**：Spring Boot 3.2.0 + MySQL + Playwright  
**Java版本**：JDK 17  
**默认端口**：8080  
**API基础路径**：`/api`

---

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 用途 | 下载地址 |
|------|---------|------|----------|
| JDK | 17+ | Java运行环境 | https://adoptium.net/ |
| Maven | 3.8+ | 项目构建工具 | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | 数据库 | https://dev.mysql.com/downloads/mysql/ |

### 1.2 可选软件

| 软件 | 用途 | 下载地址 |
|------|------|----------|
| IntelliJ IDEA | 开发IDE（推荐） | https://www.jetbrains.com/idea/download/ |
| Navicat/DBeaver | 数据库管理工具 | https://www.navicat.com.cn/ |

### 1.3 环境变量配置

**Windows系统：**

```powershell
# JAVA_HOME
JAVA_HOME=C:\Program Files\Java\jdk-17
Path添加: %JAVA_HOME%\bin

# MAVEN_HOME
MAVEN_HOME=C:\Program Files\Apache\maven
Path添加: %MAVEN_HOME%\bin
```

**验证安装：**

```powershell
java -version    # 应显示 17.x.x
mvn -version     # 应显示 3.8.x 或更高
mysql --version  # 应显示 8.0.x 或更高
```

---

## 二、数据库配置

### 2.1 创建数据库

```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE hotel_price_monitor DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选，建议使用独立用户）
CREATE USER 'knowprice'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON hotel_price_monitor.* TO 'knowprice'@'localhost';
FLUSH PRIVILEGES;
```

### 2.2 数据库表结构

数据库表会在首次启动时自动创建（`spring.jpa.hibernate.ddl-auto=update`），包括：

| 表名 | 说明 |
|------|------|
| hotels | 酒店信息表 |
| room_types | 房型信息表 |
| price_records | 价格记录表 |
| reports | 报告表 |
| collection_configs | 采集配置表 |
| llm_configs | LLM配置表 |
| prompt_templates | 提示词模板表 |

---

## 三、项目配置

### 3.1 配置文件位置

```
backend/src/main/resources/application.properties
```

### 3.2 核心配置项

```properties
# ==================== 服务配置 ====================
server.port=8080
server.servlet.context-path=/api

# ==================== 数据库配置 ====================
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_price_monitor?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

# ==================== JPA配置 ====================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# ==================== 日志配置 ====================
logging.level.com.knowprice=info

# ==================== Playwright配置 ====================
playwright.headless=true

# ==================== LLM配置 ====================
llm.enabled=false
llm.api-endpoint=https://api.minimax.chat/v1/text/chatcompletions
llm.model=minimax2.5
llm.temperature=0.7
llm.max-tokens=2000
```

### 3.3 配置项说明

| 配置项 | 说明 | 默认值 | 是否必须修改 |
|--------|------|--------|-------------|
| server.port | 服务端口 | 8080 | 否 |
| spring.datasource.url | 数据库连接地址 | localhost:3306 | 是（根据实际情况） |
| spring.datasource.username | 数据库用户名 | root | 是 |
| spring.datasource.password | 数据库密码 | root | 是 |
| playwright.headless | 是否无头模式运行浏览器 | true | 否 |
| llm.enabled | 是否启用LLM功能 | false | 否 |

---

## 四、Playwright配置（价格采集功能）

### 4.1 安装Playwright浏览器

首次运行采集功能前，需要安装Playwright浏览器：

```powershell
# 进入项目目录
cd backend

# 安装Playwright浏览器
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

或者使用已安装的Maven：

```powershell
# 下载Playwright CLI依赖后执行
java -jar playwright-cli.jar install chromium
```

### 4.2 Playwright配置说明

| 配置项 | 说明 |
|--------|------|
| `playwright.headless=true` | 无头模式，不显示浏览器窗口，适合服务器环境 |
| `playwright.headless=false` | 有头模式，显示浏览器窗口，适合调试 |

---

## 五、启动项目

### 5.1 方式一：使用Maven命令（推荐开发环境）

```powershell
# 进入项目目录
cd backend

# 编译项目
mvn clean compile

# 启动项目
mvn spring-boot:run
```

### 5.2 方式二：打包后运行（推荐生产环境）

```powershell
# 进入项目目录
cd backend

# 打包项目（跳过测试）
mvn clean package -DskipTests

# 运行JAR包
java -jar target/hotel-price-monitor-1.0.0.jar
```

### 5.3 方式三：使用IDE启动

**IntelliJ IDEA：**

1. 打开项目 `backend` 目录
2. 等待Maven依赖下载完成
3. 找到 `HotelPriceMonitorApplication.java`
4. 右键 → Run 'HotelPriceMonitorApplication'

### 5.4 方式四：使用Maven Wrapper（无需安装Maven）

```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

---

## 六、验证启动

### 6.1 检查服务状态

```powershell
# 访问健康检查端点
curl http://localhost:8080/api/actuator/health

# 预期返回
{"status":"UP"}
```

### 6.2 测试API接口

```powershell
# 测试获取酒店列表
curl http://localhost:8080/api/hotels

# 预期返回
{"code":200,"message":"获取成功","data":[]}
```

### 6.3 查看启动日志

成功启动后会看到类似日志：

```
Started HotelPriceMonitorApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

## 七、常见问题排查

### 7.1 端口被占用

**错误信息：**
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案：**
```powershell
# 查看占用端口的进程
netstat -ano | findstr :8080

# 结束进程（PID为查询到的进程ID）
taskkill /PID <PID> /F

# 或修改application.properties中的端口
server.port=8081
```

### 7.2 数据库连接失败

**错误信息：**
```
Communications link failure
```

**排查步骤：**
1. 确认MySQL服务已启动
2. 检查数据库连接地址、用户名、密码是否正确
3. 检查防火墙是否阻止连接
4. 确认数据库已创建

```powershell
# Windows检查MySQL服务
net start | findstr MySQL

# 启动MySQL服务
net start MySQL80
```

### 7.3 Playwright浏览器未安装

**错误信息：**
```
Executable doesn't exist
```

**解决方案：**
```powershell
# 安装Chromium浏览器
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### 7.4 Java版本不匹配

**错误信息：**
```
Unsupported class file major version 61
```

**解决方案：**
- 确保使用JDK 17或更高版本
- 检查JAVA_HOME环境变量配置

### 7.5 Maven依赖下载失败

**解决方案：**
```powershell
# 清理本地仓库缓存
mvn dependency:purge-local-repository

# 强制更新依赖
mvn clean install -U
```

---

## 八、生产环境部署建议

### 8.1 配置优化

```properties
# 生产环境配置示例
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.knowprice=warn
playwright.headless=true
```

### 8.2 JVM参数调优

```powershell
java -Xms512m -Xmx1024m -jar hotel-price-monitor-1.0.0.jar
```

### 8.3 后台运行（Linux）

```bash
# 使用nohup后台运行
nohup java -jar hotel-price-monitor-1.0.0.jar > app.log 2>&1 &

# 查看日志
tail -f app.log
```

### 8.4 Windows服务部署

推荐使用 `winsw` 或 `nssm` 将应用注册为Windows服务。

---

## 九、API接口列表

### 9.1 酒店管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /hotels | 获取所有酒店 |
| GET | /hotels/{id} | 获取单个酒店 |
| GET | /hotels/own | 获取自有酒店 |
| POST | /hotels | 创建酒店 |
| PUT | /hotels/{id} | 更新酒店 |
| DELETE | /hotels/{id} | 删除酒店 |

### 9.2 价格采集

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /collection/trigger | 触发全平台采集 |
| POST | /collection/trigger/{platform} | 触发指定平台采集 |
| GET | /collection/records | 获取价格记录 |
| GET | /collection/config | 获取采集配置 |
| POST | /collection/config | 保存采集配置 |

### 9.3 报告管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /reports | 获取报告列表 |
| GET | /reports/{id} | 获取单个报告 |
| GET | /reports/date/{date} | 按日期获取报告 |
| POST | /reports/generate | 生成报告 |
| DELETE | /reports/{id} | 删除报告 |

### 9.4 LLM配置

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /llm/config | 获取LLM配置 |
| POST | /llm/config | 保存LLM配置 |
| POST | /llm/test | 测试LLM连接 |
| GET | /llm/enabled | 检查LLM是否启用 |

---

## 十、项目结构

```
backend/
├── src/
│   └── main/
│       ├── java/com/knowprice/
│       │   ├── controller/          # 控制器层
│       │   ├── service/             # 服务层
│       │   ├── repository/          # 数据访问层
│       │   ├── entity/              # 实体类
│       │   ├── collector/           # 价格采集器
│       │   ├── factory/             # 工厂类
│       │   ├── scheduler/           # 定时任务
│       │   └── HotelPriceMonitorApplication.java
│       └── resources/
│           ├── prompts/             # LLM提示词模板
│           └── application.properties
├── pom.xml
└── mvnw / mvnw.cmd                  # Maven Wrapper
```
