# 前端项目启动手册

## 项目概述

**项目名称**：酒店价格监控系统前端应用  
**技术栈**：Vue 3 + Vite + Element Plus + ECharts  
**Node.js版本**：16.0+（推荐18.0+）  
**默认端口**：3000  
**API代理地址**：`http://localhost:8080`

---

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 用途 | 下载地址 |
|------|---------|------|----------|
| Node.js | 16.0+（推荐18.0+） | JavaScript运行环境 | https://nodejs.org/ |
| npm | 8.0+（随Node.js安装） | 包管理器 | 随Node.js安装 |

### 1.2 可选软件

| 软件 | 用途 | 下载地址 |
|------|------|----------|
| VS Code | 开发IDE（推荐） | https://code.visualstudio.com/ |
| pnpm | 更快的包管理器 | https://pnpm.io/ |
| yarn | 替代包管理器 | https://yarnpkg.com/ |

### 1.3 环境变量配置

**Windows系统：**

Node.js安装后会自动配置环境变量。

**验证安装：**

```powershell
node -v    # 应显示 v18.x.x 或更高
npm -v     # 应显示 9.x.x 或更高
```

### 1.4 npm镜像配置（国内用户推荐）

```powershell
# 切换到淘宝镜像
npm config set registry https://registry.npmmirror.com

# 验证镜像配置
npm config get registry

# 如需恢复官方镜像
npm config set registry https://registry.npmjs.org
```

---

## 二、项目依赖安装

### 2.1 进入项目目录

```powershell
cd frontend
```

### 2.2 安装依赖

**使用npm：**

```powershell
npm install
```

**使用pnpm（更快）：**

```powershell
# 先安装pnpm
npm install -g pnpm

# 安装依赖
pnpm install
```

**使用yarn：**

```powershell
# 先安装yarn
npm install -g yarn

# 安装依赖
yarn install
```

### 2.3 依赖说明

| 依赖包 | 版本 | 用途 |
|--------|------|------|
| vue | ^3.3.4 | Vue 3框架 |
| vue-router | ^4.2.4 | 路由管理 |
| element-plus | ^2.3.12 | UI组件库 |
| axios | ^1.6.0 | HTTP请求库 |
| echarts | ^5.4.3 | 图表库 |
| dayjs | ^1.11.10 | 日期处理 |
| vite | ^4.4.9 | 构建工具 |

---

## 三、项目配置

### 3.1 配置文件位置

```
frontend/vite.config.js
```

### 3.2 核心配置说明

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,                    // 开发服务器端口
    proxy: {
      '/api': {                    // API代理配置
        target: 'http://localhost:8080',  // 后端服务地址
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
```

### 3.3 配置项说明

| 配置项 | 说明 | 默认值 | 是否需要修改 |
|--------|------|--------|-------------|
| server.port | 前端开发服务器端口 | 3000 | 否 |
| server.proxy['/api'].target | 后端API地址 | http://localhost:8080 | 是（根据后端实际地址） |

### 3.4 API配置

API基础配置位于 `src/utils/api.js`：

```javascript
const apiClient = axios.create({
  baseURL: '/api',      // API基础路径
  timeout: 30000,       // 请求超时时间（30秒）
  headers: {
    'Content-Type': 'application/json'
  }
})
```

---

## 四、启动项目

### 4.1 开发模式启动

```powershell
# 进入项目目录
cd frontend

# 启动开发服务器
npm run dev
```

启动成功后会显示：

```
  VITE v4.4.9  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: http://192.168.x.x:3000/
```

### 4.2 访问应用

打开浏览器访问：http://localhost:3000

### 4.3 生产环境构建

```powershell
# 构建生产版本
npm run build

# 构建产物位于 dist/ 目录
```

### 4.4 预览生产构建

```powershell
# 预览构建结果
npm run preview
```

---

## 五、开发环境配置

### 5.1 VS Code推荐扩展

| 扩展名称 | 用途 |
|----------|------|
| Vue - Official | Vue语法支持 |
| ESLint | 代码规范检查 |
| Prettier | 代码格式化 |
| Volar | Vue 3智能提示 |

### 5.2 推荐settings.json配置

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "[vue]": {
    "editor.defaultFormatter": "Vue.volar"
  }
}
```

---

## 六、验证启动

### 6.1 检查前端服务

```powershell
# 访问前端页面
curl http://localhost:3000

# 预期返回HTML内容
```

### 6.2 检查API代理

确保后端服务已启动（端口8080），然后：

```powershell
# 通过前端代理访问后端API
curl http://localhost:3000/api/hotels

# 预期返回
{"code":200,"message":"获取成功","data":[]}
```

### 6.3 浏览器控制台检查

1. 打开浏览器开发者工具（F12）
2. 切换到 Console 标签
3. 确认无红色错误信息
4. 切换到 Network 标签
5. 刷新页面，确认API请求正常

---

## 七、常见问题排查

### 7.1 端口被占用

**错误信息：**
```
Port 3000 is in use, trying another one...
```

**解决方案：**
```powershell
# 查看占用端口的进程
netstat -ano | findstr :3000

# 结束进程
taskkill /PID <PID> /F

# 或修改vite.config.js中的端口
server: {
  port: 3001
}
```

### 7.2 依赖安装失败

**错误信息：**
```
npm ERR! network request failed
```

**解决方案：**
```powershell
# 清理npm缓存
npm cache clean --force

# 删除node_modules目录
rmdir /s /q node_modules

# 删除package-lock.json
del package-lock.json

# 重新安装
npm install
```

### 7.3 Node版本过低

**错误信息：**
```
error: The engine "node" is incompatible
```

**解决方案：**
- 升级Node.js到16.0或更高版本
- 或使用nvm管理多个Node版本

```powershell
# 使用nvm切换Node版本
nvm install 18
nvm use 18
```

### 7.4 API请求跨域

**错误信息：**
```
Access to XMLHttpRequest at '...' from origin '...' has been blocked by CORS policy
```

**解决方案：**
1. 确认Vite代理配置正确
2. 确认后端已启动
3. 检查后端是否配置了CORS

后端已配置CORS（在Controller中）：
```java
@CrossOrigin(origins = "*")
```

### 7.5 页面空白

**排查步骤：**
1. 打开浏览器控制台查看错误
2. 检查API请求是否正常
3. 检查路由配置是否正确

---

## 八、生产环境部署

### 8.1 构建生产版本

```powershell
# 构建
npm run build

# 产物位于 dist/ 目录
```

### 8.2 部署到Nginx

**Nginx配置示例：**

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 8.3 部署到静态服务器

将 `dist/` 目录内容上传到任意静态文件服务器即可。

### 8.4 环境变量配置

创建 `.env.production` 文件：

```
VITE_API_BASE_URL=https://api.your-domain.com
```

---

## 九、项目结构

```
frontend/
├── src/
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── utils/
│   │   └── api.js            # API接口封装
│   ├── views/
│   │   ├── Home.vue          # 首页
│   │   ├── HotelSetting.vue  # 酒店设置
│   │   ├── FilterSetting.vue # 采集配置
│   │   ├── ReportView.vue    # 报告查看
│   │   └── LLMSettings.vue   # LLM配置
│   ├── App.vue               # 根组件
│   └── main.js               # 入口文件
├── dist/                     # 构建产物
├── index.html                # HTML模板
├── package.json              # 项目配置
├── vite.config.js            # Vite配置
└── package-lock.json         # 依赖锁定
```

---

## 十、功能模块说明

### 10.1 首页 (Home.vue)

- 系统概览
- 价格趋势图表
- 最新采集数据展示

### 10.2 酒店设置 (HotelSetting.vue)

- 添加/编辑/删除酒店
- 管理房型信息
- 标记自有酒店

### 10.3 采集配置 (FilterSetting.vue)

- 配置采集区域
- 设置星级筛选
- 配置定时任务

### 10.4 报告查看 (ReportView.vue)

- 查看历史报告
- 生成新报告
- 导出报告数据

### 10.5 LLM配置 (LLMSettings.vue)

- 配置大模型API
- 测试连接
- 管理提示词模板

---

## 十一、开发命令汇总

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 构建生产版本 |
| `npm run preview` | 预览生产构建 |
| `npm install` | 安装依赖 |
| `npm update` | 更新依赖 |
| `npm outdated` | 检查过期依赖 |

---

## 十二、与后端联调

### 12.1 确保后端已启动

```powershell
# 检查后端服务
curl http://localhost:8080/api/actuator/health
```

### 12.2 启动前端

```powershell
npm run dev
```

### 12.3 验证联调

1. 访问 http://localhost:3000
2. 打开浏览器开发者工具
3. 检查Network面板，确认API请求正常
4. 测试各功能模块

---

## 十三、注意事项

1. **Node.js版本**：确保使用Node.js 16.0或更高版本
2. **后端依赖**：前端启动前需确保后端服务已启动
3. **API代理**：开发环境通过Vite代理访问后端API
4. **跨域问题**：生产环境需配置Nginx或后端CORS
5. **浏览器兼容**：推荐使用Chrome、Firefox、Edge最新版本
