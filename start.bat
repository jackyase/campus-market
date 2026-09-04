@echo off
chcp 65001 >nul
title 校园二手集市 - 一键启动

echo ============================================
echo   校园二手集市 一键启动
echo   后端 : http://localhost:8080
echo   前端 : http://localhost:5173
echo ============================================
echo.

rem 启动后端（新窗口，工作目录为项目根目录）
start "CampusMarket-Backend" /d "%~dp0" cmd /k gradlew.bat bootRun
echo [1/2] 后端启动中（新窗口），首次编译较慢请耐心等待...
timeout /t 10 /nobreak >nul

rem 启动前端（新窗口，工作目录为 frontend）
start "CampusMarket-Frontend" /d "%~dp0frontend" cmd /k npm run dev
echo [2/2] 前端启动中（新窗口）...
timeout /t 6 /nobreak >nul

rem 自动打开浏览器
start "" "http://localhost:5173"

echo.
echo 完成！浏览器已自动打开 http://localhost:5173
echo 停止服务：分别到「CampusMarket-Backend」「CampusMarket-Frontend」窗口按 Ctrl+C
echo.
pause
