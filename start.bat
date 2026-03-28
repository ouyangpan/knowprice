@echo off
echo ========================================
echo 酒店价格监控助手 - 启动脚本
echo ========================================
echo.

echo [1/4] 检查MySQL数据库...
echo 请确保MySQL已启动，数据库hotel_price_monitor已创建
echo 如果未创建，请执行 scripts/init_database.sql
echo.

echo [2/4] 启动后端服务...
echo 请在另一个终端窗口运行:
echo   cd backend
echo   mvn spring-boot:run
echo.

echo [3/4] 启动前端服务...
echo 请在另一个终端窗口运行:
echo   cd frontend
echo   npm run dev
echo.

echo [4/4] 访问应用...
echo 后端API: http://localhost:8080/api
echo 前端页面: http://localhost:3000
echo.

echo ========================================
echo 启动完成！
echo ========================================
pause
