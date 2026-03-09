# CLAUDE.md

このファイルは、Claude Code がこのリポジトリのコードを扱う際のガイダンスを提供します。

## プロジェクト概要

React/TypeScript フロントエンドと Spring Boot/Java バックエンド、MySQL データベースで構成されたフルスタック Q&A（質問回答）システム。基本設計書 v1.0.1 に基づいて実装済み（001〜014 チケット完了）。

## コマンド

### フロントエンド（`frontend/`）

```bash
npm run dev       # Vite 開発サーバー起動（ポート 5173）
npm run build     # TypeScript コンパイル + Vite ビルド
npm run lint      # ESLint
npm run preview   # プロダクションビルドのプレビュー
```

### バックエンド（`backend/`）

```bash
./mvnw spring-boot:run          # アプリケーション起動（ポート 8080）
./mvnw test                     # テスト実行
./mvnw clean install            # テスト込みフルビルド
./mvnw test -Dtest=ClassName    # 特定テストクラス実行
```

### データベース

```bash
docker compose -f backend/docker-compose.yml up -d     # MySQL 8.0 コンテナ起動（ポート 3306）
docker compose -f backend/docker-compose.yml down      # コンテナ停止
```

## アーキテクチャ

3層アーキテクチャ：React SPA → Spring Boot REST API → MySQL

```
[ ブラウザ (Chrome / Edge) ]
        ↕ HTTP (JSON / REST API)
[ フロントエンド : React + TypeScript + Vite ]
        ↕ fetch (REST API)
[ バックエンド : Java + Spring Framework ]
        ↕ JDBC / JPA
[ DB : MySQL ]
```

## フロントエンド構成（`frontend/src/`）

```
src/
├── components/       # 共通UIコンポーネント
│   ├── ErrorMessage.tsx          # エラーメッセージ表示
│   ├── DeleteConfirmDialog.tsx   # 削除確認モーダル
│   ├── AnswerFormModal.tsx       # 回答登録モーダル
│   └── AnswerEditModal.tsx       # 回答修正モーダル
├── pages/            # 画面単位のコンポーネント
│   ├── TopPage.tsx
│   ├── QuestionNewPage.tsx
│   ├── QuestionEditPage.tsx
│   ├── UnansweredListPage.tsx
│   ├── QaListPage.tsx
│   └── QuestionDetailPage.tsx
├── api/              # API通信関数
│   ├── client.ts     # ApiError クラス・apiFetch 共通関数
│   ├── questions.ts
│   └── answers.ts
├── types/            # 型定義
│   └── index.ts
├── App.tsx           # ルーティング定義
└── main.tsx
```

- ルーティング：react-router-dom v6
- API 通信：fetch API（REST）、`api/client.ts` の `apiFetch` 関数で共通化
- API ベース URL：`VITE_API_BASE_URL` 環境変数（未設定時は `http://localhost:8080`）

## バックエンド構成（`backend/src/main/java/com/example/qabackend/`）

```
com.example.qabackend/
├── controller/
│   ├── HealthController.java     # GET /health
│   ├── QuestionController.java   # 質問関連 5エンドポイント
│   └── AnswerController.java     # 回答関連 3エンドポイント
├── service/
│   ├── QuestionService.java      # 質問業務ロジック（@Transactional）
│   └── AnswerService.java        # 回答業務ロジック（@Transactional）
├── repository/
│   ├── QuestionRepository.java   # JpaRepository 継承
│   └── AnswerRepository.java     # JpaRepository 継承
├── entity/
│   ├── Question.java             # 質問エンティティ
│   ├── Answer.java               # 回答エンティティ
│   └── QuestionStatus.java       # Enum（UNANSWERED / ANSWERED）
├── dto/
│   ├── QuestionRequest.java      # 質問リクエスト DTO
│   ├── QuestionResponse.java     # 質問レスポンス DTO
│   ├── QuestionDetailResponse.java # 質問詳細レスポンス DTO（回答リスト含む）
│   ├── AnswerRequest.java        # 回答リクエスト DTO
│   ├── AnswerResponse.java       # 回答レスポンス DTO
│   └── ErrorResponse.java        # 統一エラーレスポンス DTO
├── exception/
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   └── BusinessException.java
└── WebConfig.java                # CORS 設定
```

- Javaパッケージ：`package com.example.qabackend;`
- DB設定（`application.yml`）：DB接続情報は環境変数で注入する（後述）
- Hibernate：`ddl-auto: update`（テーブル自動生成）
- CORS：`WebConfig.java` で `/api/**` に `http://localhost:5173` を許可
- Bean Validation：`pom.xml` に `spring-boot-starter-validation` 追加済み（`spring-boot-starter-web` には含まれない）
- Actuator：`spring-boot-starter-actuator` 追加済み（`/actuator/health`, `/actuator/info` を公開）

### DB接続情報の環境変数化

`application.yml` の DB接続情報は以下のように環境変数参照形式で記述すること：

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/qadb?serverTimezone=Asia/Tokyo&characterEncoding=UTF-8}
    username: ${SPRING_DATASOURCE_USERNAME:qauser}
    password: ${SPRING_DATASOURCE_PASSWORD:qapassword}
```

- DB接続情報（ユーザー名・パスワード）は環境変数で注入する
- ローカル開発では `.env` または `docker-compose.yml` の `environment` セクションで設定する
- 平文パスワードをリポジトリに直接記載しない（`.env` は `.gitignore` に追加すること）

## データベース設計

### questionsテーブル

| カラム名 | 型 | 説明 |
|----------|----|------|
| `id` | BIGINT PK AUTO_INCREMENT | 質問ID |
| `title` | VARCHAR(100) NOT NULL | 質問タイトル |
| `content` | TEXT NOT NULL | 質問内容 |
| `questioner` | VARCHAR(50) NOT NULL | 質問者名 |
| `status` | ENUM('UNANSWERED','ANSWERED') NOT NULL DEFAULT 'UNANSWERED' | ステータス |
| `created_at` | DATETIME NOT NULL | 登録日時 |
| `updated_at` | DATETIME NOT NULL | 更新日時 |
| `deleted_at` | DATETIME NULL DEFAULT NULL | 論理削除日時 |

### answersテーブル

| カラム名 | 型 | 説明 |
|----------|----|------|
| `id` | BIGINT PK AUTO_INCREMENT | 回答ID |
| `question_id` | BIGINT FK(questions.id) NOT NULL | 紐づく質問ID |
| `content` | TEXT NOT NULL | 回答内容 |
| `responder` | VARCHAR(50) NOT NULL | 回答者名 |
| `created_at` | DATETIME NOT NULL | 登録日時 |
| `updated_at` | DATETIME NOT NULL | 更新日時 |
| `deleted_at` | DATETIME NULL DEFAULT NULL | 論理削除日時 |

- MySQL はDocker（`backend/docker-compose.yml`）で起動、charset utf8mb4、timezone Asia/Tokyo
- 両テーブルとも論理削除（`deleted_at`）を採用。物理削除は行わない

### タイムスタンプ管理方針

- `created_at` / `updated_at` はアプリケーション側（JPA）で管理する
- `created_at`：エンティティ保存時（`@PrePersist`）に現在時刻を設定する
- `updated_at`：エンティティ更新時（`@PreUpdate`）に現在時刻を設定する
- DB側の `ON UPDATE CURRENT_TIMESTAMP` は使用しない
- タイムゾーンは Asia/Tokyo で統一する

## API設計

API はリソース指向 REST 設計。URLはリソース名（名詞）で構成し、操作は HTTP メソッドで表現する。

| # | メソッド | エンドポイント | 説明 |
|---|----------|---------------|------|
| 1 | GET | `/api/questions` | 質問一覧取得（検索・絞り込み・ソート） |
| 2 | GET | `/api/questions/{id}` | 質問詳細取得（回答含む） |
| 3 | POST | `/api/questions` | 質問新規登録 |
| 4 | PUT | `/api/questions/{id}` | 質問更新 |
| 5 | DELETE | `/api/questions/{id}` | 質問削除（論理削除） |
| 6 | POST | `/api/questions/{id}/answers` | 回答登録 |
| 7 | PUT | `/api/answers/{answerId}` | 回答更新 |
| 8 | DELETE | `/api/answers/{answerId}` | 回答削除（論理削除） |

### GET /api/questions クエリパラメータ

| パラメータ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `keyword` | string | なし | タイトル・質問内容の LIKE 検索 |
| `status` | `UNANSWERED` \| `ANSWERED` | なし（全件） | ステータス絞り込み |
| `sort` | `asc` \| `desc` | `desc` | 質問登録日時のソート順 |

### HTTPステータスコード

| コード | 説明 |
|--------|------|
| 200 OK | GET / PUT / DELETE 成功時 |
| 201 Created | POST 成功時 |
| 400 Bad Request | 入力値不正・バリデーションエラー |
| 404 Not Found | IDが存在しない、または論理削除済み |
| 500 Internal Server Error | 予期しない例外（スタックトレースはレスポンスに含めない） |

### エラーレスポンス統一フォーマット

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "入力内容に誤りがあります。",
  "timestamp": "2026-03-01T10:00:00",
  "fieldErrors": [
    { "field": "title", "message": "タイトルは必須です。" }
  ]
}
```

- エラーハンドラーは `@ControllerAdvice` で一元管理する
- `fieldErrors` はバリデーションエラー時のみ付与。フロントエンドは項目単位でエラーを表示する

## 業務ロジック

### ステータス自動更新

- フロントエンドから `status` を送信してはならない。ステータスはサーバー側でのみ更新する
- 回答登録時：有効回答数が 0 → 1 になる場合、質問の `status` を `ANSWERED` に更新
- 回答削除時：有効回答数が 1 → 0 になる場合、質問の `status` を `UNANSWERED` に戻す
- 「有効回答数」＝`deleted_at IS NULL` の回答数

### 回答最大3件制御

- バックエンドで有効回答数が 3 件以上の場合は `400 Bad Request`（「回答は最大3件までです。」）
- フロントエンドでも回答数が 3 件の場合は「回答を追加」ボタンを非表示または非活性にする

### 論理削除

- 全取得系APIで `deleted_at IS NULL` を WHERE 条件に必ず付与する
- 論理削除済みリソースへの GET / PUT / DELETE は `404 Not Found` を返す
- 質問削除時は紐づく全回答（`deleted_at IS NULL`）も同一トランザクション内で論理削除する

### トランザクション管理

- サービス層に `@Transactional` を付与する
- 質問削除（質問 + 全回答の論理削除）は必ず同一トランザクション内で処理する
- 同時更新は Last Write Wins（楽観ロックは v1.0.0 では未実装）

## バリデーション設計

| フィールド | 最大文字数 | 必須 |
|-----------|-----------|:----:|
| タイトル | 100文字 | ○ |
| 質問内容 | 500文字 | ○ |
| 質問者 | 50文字 | ○ |
| 回答内容 | 500文字 | ○ |
| 回答者 | 50文字 | ○ |

- フロントエンド・バックエンドの両方でバリデーションを実施する（バックエンドは必須）
- エラーメッセージ例：「タイトルは必須です。」「タイトルは100文字以内で入力してください。」

## 画面一覧

| 画面ID | 画面名 | パス |
|--------|--------|------|
| P-01 | TOPページ | `/` |
| P-02 | 質問登録画面 | `/questions/new` |
| P-03 | 回答対象一覧画面 | `/questions/unanswered` |
| P-04 | 質問詳細・回答画面 | `/questions/:id` |
| P-05 | Q&A 一覧画面 | `/qa` |
| P-06 | 質問修正画面 | `/questions/:id/edit` |

- 質問登録（P-02）完了後は `/qa` へリダイレクト
- 質問更新（P-06）完了後は `/questions/:id` へリダイレクト
- TOP 以外の全画面に「戻る」ボタンを配置。押下時は `/` へ遷移（`history.back` は使用しない）
- 削除確認ダイアログはブラウザ標準の `confirm` を使用せず、独自モーダル（DeleteConfirmDialog）を実装済み

## 実装パターン

### エンティティ

```java
// status は文字列ではなく Java Enum で定義する
public enum QuestionStatus { UNANSWERED, ANSWERED }

// タイムスタンプは @PrePersist / @PreUpdate で管理する
@PrePersist
void onCreate() { this.createdAt = this.updatedAt = LocalDateTime.now(); }

@PreUpdate
void onUpdate() { this.updatedAt = LocalDateTime.now(); }
```

- `status` フィールドには `@Enumerated(EnumType.STRING)` を付与する
- フィールドの `length` は DB設計と一致させること（例：title は 100）

### CORS 設定

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### バリデーション（Bean Validation）

```java
// リクエスト DTO に付与する
@NotBlank(message = "タイトルは必須です。")
@Size(max = 100, message = "タイトルは100文字以内で入力してください。")
private String title;
```

- コントローラーの引数に `@Valid` を付与して有効化する
- `@ControllerAdvice` で `MethodArgumentNotValidException` をハンドリングし、統一エラー形式で返す

### DTO 変換

- Entity → ResponseDTO の変換はサービス層またはコンストラクタで行う
- RequestDTO → Entity はサービス層で行う
- MapStruct 等のマッパーライブラリは MVP では使用しない（シンプルに手動変換）

## Repository 実装方針

- Spring Data JPA を使用する
- Repository は JpaRepository を継承する
- NativeQuery は MVP では使用しない
- 取得系は deleted_at IS NULL を必ず条件に含める

## Controller 設計ルール

- Controller は DTO のみ扱う
- Entity を直接レスポンスとして返さない
- RequestDTO → Service → Entity
- Entity → Service → ResponseDTO

## セキュリティ

- SQLインジェクション対策：プリペアドステートメント / JPA を使用
- XSS対策：フロントエンドは React の JSX レンダリングで基本対応
- エラー時にスタックトレースをレスポンスに含めない（`server.error.include-stacktrace=never`）
- 認証・認可は MVP 対象外

## 対象外（Out of Scope）

- ユーザー認証・認可
- ページネーション（MVPでは全件取得）
- 論理削除データの復元機能（v1.1.0 以降の検討対象）
- クラウド環境での稼働
- スマートフォン最適化
- カテゴリ機能・添付ファイル・メール通知・Excel出力
