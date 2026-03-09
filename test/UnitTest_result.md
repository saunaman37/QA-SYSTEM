# 単体テスト 実施結果

## 実施概要

| 項目 | 内容 |
|-----|------|
| 実施日 | 2026-03-10 |
| バックエンド実行コマンド | `./mvnw test -Dtest="QuestionServiceTest,AnswerServiceTest,GlobalExceptionHandlerTest,QuestionControllerTest,AnswerControllerTest"` |
| フロントエンド実行コマンド | `npx vitest run --reporter=verbose` |
| バックエンド結果 | **62件 PASS / 0件 FAIL** |
| フロントエンド結果 | **7件 PASS / 0件 FAIL** |
| 合計 | **69件 PASS / 0件 FAIL** |

---

## 環境・特記事項

| 項目 | 内容 |
|-----|------|
| Spring Boot | 4.0.3（Spring Framework 7.x）|
| Java | 21 |
| JUnit | 5（spring-boot-starter-test に同梱） |
| Mockito | spring-boot-starter-test に同梱 |
| テスト用 DB | H2（インメモリ、`MODE=MySQL`、pom.xml に test スコープで追加） |
| Controller テスト方式 | `MockMvcBuilders.standaloneSetup()` ※ Spring Boot 4.x で `@WebMvcTest` 廃止のため |
| Jackson | 3.0.4（`tools.jackson.databind.ObjectMapper` に移行済み） |
| フロントエンド | Vitest 4.0.18 + jsdom |

> **Spring Boot 4.x 対応メモ**
> - `@WebMvcTest` が spring-boot-test-autoconfigure から削除済み → `MockMvcBuilders.standaloneSetup()` に変更
> - Jackson 3.x でパッケージが `com.fasterxml.jackson.databind` → `tools.jackson.databind` に変更
> - `@MockBean` → `@MockitoBean`（今回は standaloneSetup のため不使用）

---

## バックエンド テスト結果

### 1. QuestionService（21件）

| # | テストメソッド | 結果 | 備考 |
|---|--------------|------|------|
| 1 | `getQuestions_noParams_returnsAll` | **PASS** | |
| 2 | `getQuestions_withKeyword_returnsFiltered` | **PASS** | |
| 3 | `getQuestions_withStatus_returnsFiltered` | **PASS** | |
| 4 | `getQuestions_withKeywordAndStatus_returnsFiltered` | **PASS** | |
| 5 | `getQuestions_sortAsc_appliesAscSort` | **PASS** | |
| 6 | `getQuestions_sortDesc_appliesDescSort` | **PASS** | |
| 7 | `getQuestions_invalidSort_throwsBusinessException` | **PASS** | |
| 8 | `getQuestions_invalidStatus_throwsBusinessException` | **PASS** | |
| 9 | `getQuestion_found_returnsDetail` | **PASS** | |
| 10 | `getQuestion_notFound_throwsResourceNotFoundException` | **PASS** | |
| 11 | `createQuestion_valid_returnsResponse` | **PASS** | |
| 12 | `updateQuestion_found_returnsUpdated` | **PASS** | |
| 13 | `updateQuestion_notFound_throwsResourceNotFoundException` | **PASS** | |
| 14 | `deleteQuestion_softDeletesQuestionAndAnswers` | **PASS** | |
| 15 | `deleteQuestion_notFound_throwsResourceNotFoundException` | **PASS** | |
| 51 | `getQuestions_emptyKeyword_treatedAsNoFilter` | **PASS** | 境界値：`""` はキーワードとして扱わない |
| 52 | `getQuestions_blankKeyword_treatedAsNoFilter` | **PASS** | 境界値：`"   "` はキーワードとして扱わない |
| 53 | `getQuestions_noMatchingResults_returnsEmptyList` | **PASS** | 0件時に空リスト返却、例外なし |
| 54 | `getQuestions_nullSort_defaultsToDesc` | **PASS** | sort=null のデフォルト動作確認 |
| 55 | `createQuestion_capturesEntityWithCorrectFields` | **PASS** | ArgumentCaptor で Entity フィールド確認 |
| 56 | `updateQuestion_capturesEntityWithUpdatedFields` | **PASS** | ArgumentCaptor で更新フィールド確認 |

**小計：21件 PASS / 0件 FAIL**

---

### 2. AnswerService（11件）

| # | テストメソッド | 結果 | 備考 |
|---|--------------|------|------|
| 16 | `createAnswer_firstAnswer_setsQuestionStatusToAnswered` | **PASS** | ArgumentCaptor でステータス更新確認 |
| 17 | `createAnswer_secondAnswer_statusRemainsAnswered` | **PASS** | 2回目以降は質問ステータス変更なし |
| 18 | `createAnswer_questionNotFound_throwsResourceNotFoundException` | **PASS** | |
| 19 | `createAnswer_maxAnswers_throwsBusinessException` | **PASS** | メッセージ「回答は最大3件までです。」確認 |
| 20 | `updateAnswer_valid_returnsUpdated` | **PASS** | content, responder の変更確認 |
| 21 | `updateAnswer_answerNotFound_throwsResourceNotFoundException` | **PASS** | |
| 22 | `updateAnswer_whenParentQuestionSoftDeleted_throwsResourceNotFoundException` | **PASS** | 論理削除済み質問に紐づく回答の更新不可仕様確認 |
| 23 | `deleteAnswer_lastAnswer_setsQuestionStatusToUnanswered` | **PASS** | deletedAt 設定 + ステータス UNANSWERED 確認 |
| 24 | `deleteAnswer_notLastAnswer_statusRemainsAnswered` | **PASS** | 残回答ありの場合はステータス変更なし |
| 25 | `deleteAnswer_answerNotFound_throwsResourceNotFoundException` | **PASS** | |
| 57 | `deleteAnswer_lastAnswer_verifiesQuestionSavedWithUnanswered` | **PASS** | questionRepository.save の引数確認 |

**小計：11件 PASS / 0件 FAIL**

---

### 3. QuestionController（15件）

| # | テストメソッド | 結果 | 備考 |
|---|--------------|------|------|
| 26 | `getQuestions_returns200` | **PASS** | HTTP 200, JSON配列2要素 |
| 27 | `getQuestion_exists_returns200` | **PASS** | id, title, status, answers[] 確認 |
| 28 | `getQuestion_notExists_returns404` | **PASS** | status=404, error="Not Found" 確認 |
| 29 | `createQuestion_valid_returns201` | **PASS** | HTTP 201, id, title, status 確認 |
| 30 | `createQuestion_missingTitle_returns400WithFieldError` | **PASS** | fieldErrors[title] 確認 |
| 31 | `createQuestion_emptyContent_returns400WithFieldError` | **PASS** | fieldErrors[content] 確認 |
| 32 | `createQuestion_titleTooLong_returns400WithFieldError` | **PASS** | 101文字で fieldErrors[title] 確認 |
| 33 | `updateQuestion_valid_returns200` | **PASS** | id, title 確認 |
| 34 | `updateQuestion_notExists_returns404` | **PASS** | status=404 確認 |
| 35 | `deleteQuestion_exists_returns200` | **PASS** | message="質問を削除しました。" 確認 |
| 36 | `deleteQuestion_notExists_returns404` | **PASS** | status=404 確認 |
| 58 | `createQuestion_missingQuestioner_returns400WithFieldError` | **PASS** | fieldErrors[questioner] 確認 |
| 59 | `createQuestion_blankTitle_returns400WithFieldError` | **PASS** | 空白のみ title を @NotBlank で拒否確認 |
| 60 | `createQuestion_blankContent_returns400WithFieldError` | **PASS** | 空白のみ content を @NotBlank で拒否確認 |
| 61 | `updateQuestion_invalidInput_returns400WithFieldError` | **PASS** | 更新系バリデーション有効確認 |

**小計：15件 PASS / 0件 FAIL**

---

### 4. AnswerController（11件）

| # | テストメソッド | 結果 | 備考 |
|---|--------------|------|------|
| 37 | `createAnswer_valid_returns201` | **PASS** | HTTP 201, id, questionId, content 確認 |
| 38 | `createAnswer_missingContent_returns400WithFieldError` | **PASS** | fieldErrors[content] 確認 |
| 39 | `createAnswer_questionNotFound_returns404` | **PASS** | status=404, error="Not Found" 確認 |
| 40 | `createAnswer_maxAnswers_returns400` | **PASS** | message="回答は最大3件までです。" 確認 |
| 41 | `updateAnswer_valid_returns200` | **PASS** | id, content 確認 |
| 42 | `updateAnswer_answerNotFound_returns404` | **PASS** | status=404 確認 |
| 43 | `deleteAnswer_valid_returns200` | **PASS** | message="回答を削除しました。" 確認 |
| 44 | `deleteAnswer_notFound_returns404` | **PASS** | status=404 確認 |
| 62 | `createAnswer_missingResponder_returns400WithFieldError` | **PASS** | fieldErrors[responder] 確認 |
| 63 | `createAnswer_blankContent_returns400WithFieldError` | **PASS** | 空白のみ content を @NotBlank で拒否確認 |
| 64 | `updateAnswer_invalidInput_returns400WithFieldError` | **PASS** | 更新系バリデーション有効確認 |

**小計：11件 PASS / 0件 FAIL**

---

### 5. GlobalExceptionHandler（4件）

| # | テストメソッド | 結果 | 備考 |
|---|--------------|------|------|
| 45 | `handleResourceNotFoundException_returns404` | **PASS** | status=404, error="Not Found", message, timestamp 確認 |
| 46 | `handleBusinessException_returns400` | **PASS** | status=400, error="Bad Request", message, timestamp 確認 |
| 47 | `handleValidationException_returns400WithFieldErrors` | **PASS** | error="Validation Failed", fieldErrors[0].field="title" 確認 |
| 48 | `handleUnexpectedException_returns500` | **PASS** | status=500, fieldErrors=[] 確認 |

**小計：4件 PASS / 0件 FAIL**

---

## フロントエンド テスト結果

### 6. API クライアント・ラッパー関数（7件）

| # | テストファイル | テストメソッド | 結果 | 備考 |
|---|------------|--------------|------|------|
| 49 | `client.test.ts` | `apiFetch_success_returnsData` | **PASS** | 200 OK でデータが返ることを確認 |
| 50 | `client.test.ts` | `apiFetch_error_throwsApiError` | **PASS** | ApiError のスロー・status・error・message 確認 ※1 |
| 65 | `client.test.ts` | `apiFetch_networkError_throwsOriginalError` | **PASS** | TypeError がそのまま伝播することを確認 |
| 66 | `questions.test.ts` | `getQuestions_withAllParams_buildsCorrectUrl` | **PASS** | keyword/status/sort のクエリ文字列組み立て確認 |
| 67 | `questions.test.ts` | `getQuestions_noParams_callsBaseUrl` | **PASS** | パラメータなし時にクエリ文字列なしで呼ばれることを確認 |
| 68 | `questions.test.ts` | `createQuestion_sendsPostWithJsonBody` | **PASS** | method=POST, body, Content-Type 確認 |
| 69 | `answers.test.ts` | `deleteAnswer_sendsDeleteMethod` | **PASS** | `/api/answers/1` に DELETE で呼ばれることを確認 |

> ※1 `apiFetch_error_throwsApiError` は当初 `mockResolvedValue` による Response ボディ二重読み取りで FAIL。`mockImplementation` に修正して PASS。

**小計：7件 PASS / 0件 FAIL**

---

## 最終集計

| カテゴリ | テスト件数 | PASS | FAIL |
|---------|----------|------|------|
| QuestionService | 21 | 21 | 0 |
| AnswerService | 11 | 11 | 0 |
| QuestionController | 15 | 15 | 0 |
| AnswerController | 11 | 11 | 0 |
| GlobalExceptionHandler | 4 | 4 | 0 |
| フロントエンド API | 7 | 7 | 0 |
| **合計** | **69** | **69** | **0** |

---

## 発見事項・修正記録

| 種別 | 内容 | 対応 |
|-----|------|------|
| 環境対応 | Spring Boot 4.x で `@WebMvcTest` が廃止 | `MockMvcBuilders.standaloneSetup()` + `setControllerAdvice()` に変更 |
| 環境対応 | Jackson 3.x でパッケージ名が `tools.jackson.databind` に変更 | テストコードのインポートを修正 |
| テスト修正 | `apiFetch_error_throwsApiError` で Response ボディ二重読み取りによる FAIL | `mockResolvedValue` → `mockImplementation` に変更し修正 |

---

## 人間が実施するテスト（自動化対象外）

UnitTest_h.md に詳細を記載。以下の3件は実 DB・ブラウザが必要なため手動実施。

| # | 内容 | 実施タイミング |
|---|------|-------------|
| H-1 | `deleteQuestion` の同一トランザクション・ロールバック確認（実 MySQL コンテナ） | 実 DB 環境構築後 |
| H-2 | `created_at` / `updated_at` の Asia/Tokyo タイムゾーン DB 書き込み確認 | Docker MySQL 起動後 |
| H-3 | フロントエンド画面でのフィールド単位バリデーションエラー表示確認 | 実機動作確認時 |
