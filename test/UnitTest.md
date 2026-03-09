# 単体テスト一覧

## 対象システム
React/TypeScript + Spring Boot + MySQL 構成の Q&A システム（v1.0）

## テスト方針
- バックエンド：JUnit5 + Mockito（サービス層）、MockMvc（コントローラー層）
- フロントエンド：Vitest + fetch モック（API クライアント層）
- 合計：50 件

---

## 1. QuestionService（サービス層）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 1 | `QuestionServiceTest` | `getQuestions_noParams_returnsAll` | 未削除質問が 3 件存在する | `getQuestions(null, null, null)` を呼ぶ | 3 件の `QuestionResponse` リストが返る |
| 2 | `QuestionServiceTest` | `getQuestions_withKeyword_returnsFiltered` | タイトルに「Spring」を含む質問が 2 件存在する | `getQuestions("Spring", null, null)` を呼ぶ | 2 件のリストが返る（`findByKeyword` が呼ばれる） |
| 3 | `QuestionServiceTest` | `getQuestions_withStatus_returnsFiltered` | `UNANSWERED` の質問が 1 件存在する | `getQuestions(null, "UNANSWERED", null)` を呼ぶ | 1 件のリストが返る（`findByDeletedAtIsNullAndStatus` が呼ばれる） |
| 4 | `QuestionServiceTest` | `getQuestions_withKeywordAndStatus_returnsFiltered` | 条件に合う質問が 1 件存在する | `getQuestions("Spring", "ANSWERED", null)` を呼ぶ | 1 件のリストが返る（`findByKeywordAndStatus` が呼ばれる） |
| 5 | `QuestionServiceTest` | `getQuestions_sortAsc_appliesAscSort` | 未削除質問が複数存在する | `getQuestions(null, null, "asc")` を呼ぶ | `Sort.by(ASC, "createdAt")` でリポジトリが呼ばれる |
| 6 | `QuestionServiceTest` | `getQuestions_sortDesc_appliesDescSort` | 未削除質問が複数存在する | `getQuestions(null, null, "desc")` を呼ぶ | `Sort.by(DESC, "createdAt")` でリポジトリが呼ばれる |
| 7 | `QuestionServiceTest` | `getQuestions_invalidSort_throwsBusinessException` | なし | `getQuestions(null, null, "invalid")` を呼ぶ | `BusinessException` がスローされる（メッセージに「sort」を含む） |
| 8 | `QuestionServiceTest` | `getQuestions_invalidStatus_throwsBusinessException` | なし | `getQuestions(null, "UNKNOWN", null)` を呼ぶ | `BusinessException` がスローされる（メッセージに「status」を含む） |
| 9 | `QuestionServiceTest` | `getQuestion_found_returnsDetail` | id=1 の未削除質問が存在し、回答が 2 件ある | `getQuestion(1L)` を呼ぶ | 質問情報と 2 件の回答リストを含む `QuestionDetailResponse` が返る |
| 10 | `QuestionServiceTest` | `getQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `getQuestion(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる（メッセージに「99」を含む） |
| 11 | `QuestionServiceTest` | `createQuestion_valid_returnsResponse` | なし | 有効な `QuestionRequest` を渡して `createQuestion` を呼ぶ | 保存された質問の `QuestionResponse` が返り、`status` が `UNANSWERED` である |
| 12 | `QuestionServiceTest` | `updateQuestion_found_returnsUpdated` | id=1 の未削除質問が存在する | 新しいタイトルの `QuestionRequest` を渡して `updateQuestion(1L, req)` を呼ぶ | 更新後の `QuestionResponse` が返り、タイトルが変更されている |
| 13 | `QuestionServiceTest` | `updateQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `updateQuestion(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 14 | `QuestionServiceTest` | `deleteQuestion_softDeletesQuestionAndAnswers` | id=1 の質問に未削除の回答が 2 件ある | `deleteQuestion(1L)` を呼ぶ | 質問の `deletedAt` が設定され、回答 2 件の `deletedAt` も同一時刻に設定される（同一トランザクション） |
| 15 | `QuestionServiceTest` | `deleteQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `deleteQuestion(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる |

---

## 2. AnswerService（サービス層）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 16 | `AnswerServiceTest` | `createAnswer_firstAnswer_setsQuestionStatusToAnswered` | 未削除質問（id=1）が `UNANSWERED` で存在し、有効回答数が 0 件 | 有効な `AnswerRequest` で `createAnswer(1L, req)` を呼ぶ | 回答が保存され、質問の `status` が `ANSWERED` に更新される |
| 17 | `AnswerServiceTest` | `createAnswer_secondAnswer_statusRemainsAnswered` | 未削除質問が `ANSWERED` で存在し、有効回答数が 1 件 | 有効な `AnswerRequest` で `createAnswer(1L, req)` を呼ぶ | 回答が保存され、質問の `status` は変更されない（`questionRepository.save` が呼ばれない） |
| 18 | `AnswerServiceTest` | `createAnswer_questionNotFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `createAnswer(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 19 | `AnswerServiceTest` | `createAnswer_maxAnswers_throwsBusinessException` | 有効回答数が 3 件 | `createAnswer(1L, req)` を呼ぶ | `BusinessException` がスローされる（メッセージが「回答は最大3件までです。」） |
| 20 | `AnswerServiceTest` | `updateAnswer_valid_returnsUpdated` | 未削除回答（id=1）と紐づく未削除質問が存在する | 新しい内容の `AnswerRequest` で `updateAnswer(1L, req)` を呼ぶ | 更新後の `AnswerResponse` が返り、内容が変更されている |
| 21 | `AnswerServiceTest` | `updateAnswer_answerNotFound_throwsResourceNotFoundException` | id=99 の回答が存在しない | `updateAnswer(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 22 | `AnswerServiceTest` | `updateAnswer_questionNotFound_throwsResourceNotFoundException` | 回答は存在するが、紐づく質問が論理削除済み | `updateAnswer(1L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 23 | `AnswerServiceTest` | `deleteAnswer_lastAnswer_setsQuestionStatusToUnanswered` | 有効回答数が 1 件、質問が `ANSWERED` | `deleteAnswer(1L)` を呼ぶ | 回答の `deletedAt` が設定され、質問の `status` が `UNANSWERED` に戻る |
| 24 | `AnswerServiceTest` | `deleteAnswer_notLastAnswer_statusRemainsAnswered` | 有効回答数が 2 件、質問が `ANSWERED` | `deleteAnswer(1L)` を呼ぶ | 回答の `deletedAt` が設定され、質問の `status` は変更されない |
| 25 | `AnswerServiceTest` | `deleteAnswer_answerNotFound_throwsResourceNotFoundException` | id=99 の回答が存在しない | `deleteAnswer(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる |

---

## 3. QuestionController（コントローラー層 / MockMvc）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 26 | `QuestionControllerTest` | `getQuestions_returns200` | `QuestionService.getQuestions` が 2 件のリストを返す | `GET /api/questions` | HTTP 200、JSON 配列（2 要素）が返る |
| 27 | `QuestionControllerTest` | `getQuestion_exists_returns200` | `QuestionService.getQuestion(1L)` が正常な `QuestionDetailResponse` を返す | `GET /api/questions/1` | HTTP 200、`id=1` の JSON オブジェクトが返る |
| 28 | `QuestionControllerTest` | `getQuestion_notExists_returns404` | `QuestionService.getQuestion(99L)` が `ResourceNotFoundException` をスロー | `GET /api/questions/99` | HTTP 404、`status=404` の JSON エラーレスポンスが返る |
| 29 | `QuestionControllerTest` | `createQuestion_valid_returns201` | `QuestionService.createQuestion` が正常なレスポンスを返す | 有効な JSON ボディで `POST /api/questions` | HTTP 201、作成された質問 JSON が返る |
| 30 | `QuestionControllerTest` | `createQuestion_missingTitle_returns400WithFieldError` | なし | `title` が空文字の JSON ボディで `POST /api/questions` | HTTP 400、`fieldErrors` に `title` フィールドのエラーが含まれる |
| 31 | `QuestionControllerTest` | `createQuestion_emptyContent_returns400WithFieldError` | なし | `content` が null の JSON ボディで `POST /api/questions` | HTTP 400、`fieldErrors` に `content` フィールドのエラーが含まれる |
| 32 | `QuestionControllerTest` | `createQuestion_titleTooLong_returns400WithFieldError` | なし | `title` が 101 文字の JSON ボディで `POST /api/questions` | HTTP 400、`fieldErrors` に `title` フィールドのエラーが含まれる |
| 33 | `QuestionControllerTest` | `updateQuestion_valid_returns200` | `QuestionService.updateQuestion` が更新済みレスポンスを返す | 有効な JSON ボディで `PUT /api/questions/1` | HTTP 200、更新後の質問 JSON が返る |
| 34 | `QuestionControllerTest` | `updateQuestion_notExists_returns404` | `QuestionService.updateQuestion` が `ResourceNotFoundException` をスロー | `PUT /api/questions/99` | HTTP 404、JSON エラーレスポンスが返る |
| 35 | `QuestionControllerTest` | `deleteQuestion_exists_returns200` | `QuestionService.deleteQuestion` が正常終了する | `DELETE /api/questions/1` | HTTP 200、`{"message": "質問を削除しました。"}` が返る |
| 36 | `QuestionControllerTest` | `deleteQuestion_notExists_returns404` | `QuestionService.deleteQuestion` が `ResourceNotFoundException` をスロー | `DELETE /api/questions/99` | HTTP 404、JSON エラーレスポンスが返る |

---

## 4. AnswerController（コントローラー層 / MockMvc）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 37 | `AnswerControllerTest` | `createAnswer_valid_returns201` | `AnswerService.createAnswer` が正常なレスポンスを返す | 有効な JSON ボディで `POST /api/questions/1/answers` | HTTP 201、作成された回答 JSON が返る |
| 38 | `AnswerControllerTest` | `createAnswer_missingContent_returns400WithFieldError` | なし | `content` が空文字の JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`fieldErrors` に `content` フィールドのエラーが含まれる |
| 39 | `AnswerControllerTest` | `createAnswer_questionNotFound_returns404` | `AnswerService.createAnswer` が `ResourceNotFoundException` をスロー | 有効な JSON ボディで `POST /api/questions/99/answers` | HTTP 404、JSON エラーレスポンスが返る |
| 40 | `AnswerControllerTest` | `createAnswer_maxAnswers_returns400` | `AnswerService.createAnswer` が `BusinessException` をスロー | 有効な JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`message` が「回答は最大3件までです。」の JSON が返る |
| 41 | `AnswerControllerTest` | `updateAnswer_valid_returns200` | `AnswerService.updateAnswer` が更新済みレスポンスを返す | 有効な JSON ボディで `PUT /api/answers/1` | HTTP 200、更新後の回答 JSON が返る |
| 42 | `AnswerControllerTest` | `updateAnswer_answerNotFound_returns404` | `AnswerService.updateAnswer` が `ResourceNotFoundException` をスロー | `PUT /api/answers/99` | HTTP 404、JSON エラーレスポンスが返る |
| 43 | `AnswerControllerTest` | `deleteAnswer_valid_returns200` | `AnswerService.deleteAnswer` が正常終了する | `DELETE /api/answers/1` | HTTP 200、`{"message": "回答を削除しました。"}` が返る |
| 44 | `AnswerControllerTest` | `deleteAnswer_notFound_returns404` | `AnswerService.deleteAnswer` が `ResourceNotFoundException` をスロー | `DELETE /api/answers/99` | HTTP 404、JSON エラーレスポンスが返る |

---

## 5. GlobalExceptionHandler（例外ハンドラー）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 45 | `GlobalExceptionHandlerTest` | `handleResourceNotFoundException_returns404` | なし | `ResourceNotFoundException` をハンドラーに渡す | HTTP 404、`status=404`・`error="Not Found"` の `ErrorResponse` が返る |
| 46 | `GlobalExceptionHandlerTest` | `handleBusinessException_returns400` | なし | `BusinessException("回答は最大3件までです。")` をハンドラーに渡す | HTTP 400、`status=400`・`error="Bad Request"` の `ErrorResponse` が返る |
| 47 | `GlobalExceptionHandlerTest` | `handleValidationException_returns400WithFieldErrors` | なし | `MethodArgumentNotValidException` をハンドラーに渡す | HTTP 400、`error="Validation Failed"`・`fieldErrors` に対象フィールドのエラーが含まれる |
| 48 | `GlobalExceptionHandlerTest` | `handleUnexpectedException_returns500` | なし | 予期しない `RuntimeException` をハンドラーに渡す | HTTP 500、`status=500`・`error="Internal Server Error"` の `ErrorResponse` が返る |

---

## 6. フロントエンド API クライアント（Vitest）

| # | テストファイル | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 49 | `client.test.ts` | `apiFetch_success_returnsData` | `fetch` モックが `200 OK` と `{"id":1}` を返す | `apiFetch<{id: number}>("/api/test")` を呼ぶ | `{ id: 1 }` が返る |
| 50 | `client.test.ts` | `apiFetch_error_throwsApiError` | `fetch` モックが `404` と `{"status":404,"error":"Not Found","message":"質問が見つかりません。"}` を返す | `apiFetch("/api/questions/99")` を呼ぶ | `ApiError` がスローされ、`error.status === 404`・`error.message === "質問が見つかりません。"` である |

---

## テスト実装ガイド

### バックエンドテスト環境
```
場所: backend/src/test/java/com/example/qabackend/
依存: JUnit5（spring-boot-starter-test に含む）、Mockito
```

**サービス層（例：QuestionService）**
```java
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {
    @Mock QuestionRepository questionRepository;
    @Mock AnswerRepository answerRepository;
    @InjectMocks QuestionService questionService;
}
```

**コントローラー層（例：QuestionController）**
```java
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean QuestionService questionService;
    @Autowired ObjectMapper objectMapper;
}
```

### フロントエンドテスト環境
```
場所: frontend/src/__tests__/
依存: Vitest、@vitest/globals（vitest.config.ts で globals: true）
```

**API クライアント（例：client.test.ts）**
```typescript
vi.stubGlobal('fetch', vi.fn());

afterEach(() => {
  vi.restoreAllMocks();
});
```
