@echo off
chcp 65001 >nul
echo QA System を起動しています...

REM Docker (MySQL) 起動
echo [1/3] Docker (MySQL) を起動中...
docker compose -f backend\docker-compose.yml up -d
if %errorlevel% neq 0 (
    echo Docker の起動に失敗しました。Docker Desktop が起動しているか確認してください。
    pause
    exit /b 1
)
echo Docker 起動完了

REM MySQL の準備待ち
echo MySQL の準備が整うまで待機中 (10秒)...
timeout /t 10 /nobreak >nul

REM バックエンド起動（新しいウィンドウ）
echo [2/3] バックエンド (Spring Boot) を起動中...
start "QA Backend" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run"

REM バックエンドの起動待ち
echo バックエンドの起動を待機中 (20秒)...
timeout /t 20 /nobreak >nul

REM フロントエンド起動（新しいウィンドウ）
echo [3/3] フロントエンド (Vite) を起動中...
start "QA Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo 起動完了！
echo フロントエンド: http://localhost:5173
echo バックエンド:   http://localhost:8080
echo.
echo 各ウィンドウでサーバーの起動ログを確認してください。
echo バックエンドの起動には1〜2分かかる場合があります。
pause
