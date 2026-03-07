#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "QA System を起動しています..."

# Docker (MySQL) 起動
echo "[1/3] Docker (MySQL) を起動中..."
docker compose -f "$SCRIPT_DIR/backend/docker-compose.yml" up -d
if [ $? -ne 0 ]; then
    echo "Docker の起動に失敗しました。Docker Desktop が起動しているか確認してください。"
    exit 1
fi
echo "Docker 起動完了"

# MySQL の準備待ち
echo "MySQL の準備が整うまで待機中 (10秒)..."
sleep 10

# バックエンド起動（新しいターミナルタブ）
echo "[2/3] バックエンド (Spring Boot) を起動中..."
osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR/backend' && ./mvnw spring-boot:run\""

# バックエンドの起動待ち
echo "バックエンドの起動を待機中 (20秒)..."
sleep 20

# フロントエンド起動（新しいターミナルタブ）
echo "[3/3] フロントエンド (Vite) を起動中..."
osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR/frontend' && npm run dev\""

echo ""
echo "起動完了！"
echo "フロントエンド: http://localhost:5173"
echo "バックエンド:   http://localhost:8080"
echo ""
echo "各ターミナルウィンドウでサーバーの起動ログを確認してください。"
echo "バックエンドの起動には1〜2分かかる場合があります。"
