@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
title 萝卜地牢 - 本地启动

echo ============================================
echo   萝卜地牢  Radish Pixel Dungeon
echo   正在启动桌面版
echo   （第一次编译会久一点，之后都是增量，很快）
echo ============================================
echo.

call gradlew.bat :desktop:debug --console=plain
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" goto done
echo [提示] 退出码 %RC%。
echo 如果上面出现 BUILD FAILED，就是编译失败，把上面的错误内容发我就行。
echo.
pause

:done
endlocal
exit /b 0
