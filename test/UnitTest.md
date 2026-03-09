# 単体テスト一覧

## 対象システム
React/TypeScript + Spring Boot + MySQL 構成の Q&A システム（v1.0）

## テスト方針
- バックエンド：JUnit5 + Mockito（サービス層）、MockMvc（コントローラー層）
- フロントエンド：Vitest + fetch モック（API クライアント層）
- 合計：69 件（ClaudeCode 実施 69 件 ＋ 人間実施 3 件は末尾に別記）

---

## 1. QuestionService（サービス層）

> 期待結果は戻り値・例外・公開 API の挙動ベースで記述する。
> sort / ArgumentCaptor 検証など意味のある verify は残す。

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 1 | `QuestionServiceTest` | `getQuestions_noParams_returnsAll` | 未削除質問が 3 件存在する | `getQuestions(null, null, null)` を呼ぶ | 3 件の `QuestionResponse` リストが返る |
| 2 | `QuestionServiceTest` | `getQuestions_withKeyword_returnsFiltered` | タイトルに「Spring」を含む質問が 2 件存在する | `getQuestions("Spring", null, null)` を呼ぶ | 2 件の `QuestionResponse` リストが返る |
| 3 | `QuestionServiceTest` | `getQuestions_withStatus_returnsFiltered` | `UNANSWERED` の未削除質問が 1 件存在する | `getQuestions(null, "UNANSWERED", null)` を呼ぶ | 1 件の `QuestionResponse` リストが返る |
| 4 | `QuestionServiceTest` | `getQuestions_withKeywordAndStatus_returnsFiltered` | 「Spring」を含み `ANSWERED` の未削除質問が 1 件存在する | `getQuestions("Spring", "ANSWERED", null)` を呼ぶ | 1 件の `QuestionResponse` リストが返る |
| 5 | `QuestionServiceTest` | `getQuestions_sortAsc_appliesAscSort` | 未削除質問が複数存在する | `getQuestions(null, null, "asc")` を呼ぶ | `Sort.by(ASC, "createdAt")` でリポジトリが呼ばれる |
| 6 | `QuestionServiceTest` | `getQuestions_sortDesc_appliesDescSort` | 未削除質問が複数存在する | `getQuestions(null, null, "desc")` を呼ぶ | `Sort.by(DESC, "createdAt")` でリポジトリが呼ばれる |
| 7 | `QuestionServiceTest` | `getQuestions_invalidSort_throwsBusinessException` | なし | `getQuestions(null, null, "invalid")` を呼ぶ | `BusinessException` がスローされる（メッセージに「sort」を含む） |
| 8 | `QuestionServiceTest` | `getQuestions_invalidStatus_throwsBusinessException` | なし | `getQuestions(null, "UNKNOWN", null)` を呼ぶ | `BusinessException` がスローされる（メッセージに「status」を含む） |
| 9 | `QuestionServiceTest` | `getQuestion_found_returnsDetail` | id=1 の未削除質問が存在し、回答が 2 件ある | `getQuestion(1L)` を呼ぶ | 質問情報と 2 件の回答リストを含む `QuestionDetailResponse` が返る |
| 10 | `QuestionServiceTest` | `getQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `getQuestion(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる（メッセージに「99」を含む） |
| 11 | `QuestionServiceTest` | `createQuestion_valid_returnsResponse` | なし | 有効な `QuestionRequest` を渡して `createQuestion` を呼ぶ | 保存された質問の `QuestionResponse` が返り、`status` が `UNANSWERED` である |
| 12 | `QuestionServiceTest` | `updateQuestion_found_returnsUpdated` | id=1 の未削除質問が存在する | 新しいタイトルの `QuestionRequest` を渡して `updateQuestion(1L, req)` を呼ぶ | 更新後の `QuestionResponse` が返り、タイトルが変更されている |
| 13 | `QuestionServiceTest` | `updateQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `updateQuestion(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 14 | `QuestionServiceTest` | `deleteQuestion_softDeletesQuestionAndAnswers` | id=1 の質問に未削除の回答が 2 件ある | `deleteQuestion(1L)` を呼ぶ | 質問の `deletedAt` が null でなく、関連する 2 件の回答の `deletedAt` も null でなくなる |
| 15 | `QuestionServiceTest` | `deleteQuestion_notFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `deleteQuestion(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 51 | `QuestionServiceTest` | `getQuestions_emptyKeyword_treatedAsNoFilter` | 未削除質問が 3 件存在する | `getQuestions("", null, null)` を呼ぶ | 3 件の `QuestionResponse` リストが返る（keyword 絞り込みが適用されない） |
| 52 | `QuestionServiceTest` | `getQuestions_blankKeyword_treatedAsNoFilter` | 未削除質問が 3 件存在する | `getQuestions("   ", null, null)` を呼ぶ | 3 件の `QuestionResponse` リストが返る（空白のみは keyword として扱わない） |
| 53 | `QuestionServiceTest` | `getQuestions_noMatchingResults_returnsEmptyList` | 「NoMatch」に合致する質問が存在しない | `getQuestions("NoMatch", null, null)` を呼ぶ | 空のリスト `[]` が返る（例外はスローされない） |
| 54 | `QuestionServiceTest` | `getQuestions_nullSort_defaultsToDesc` | 未削除質問が複数存在する | `getQuestions(null, null, null)` を呼ぶ | `Sort.by(DESC, "createdAt")` でリポジトリが呼ばれる |
| 55 | `QuestionServiceTest` | `createQuestion_capturesEntityWithCorrectFields` | なし | `title="T"`, `content="C"`, `questioner="Q"` の `QuestionRequest` で `createQuestion` を呼ぶ | `ArgumentCaptor` でキャプチャした `Question` の `title="T"`, `content="C"`, `questioner="Q"`, `status=UNANSWERED`, `deletedAt=null` であることを確認する |
| 56 | `QuestionServiceTest` | `updateQuestion_capturesEntityWithUpdatedFields` | id=1 の未削除質問が存在する | `title="NewT"`, `content="NewC"`, `questioner="NewQ"` の `QuestionRequest` で `updateQuestion(1L, req)` を呼ぶ | `ArgumentCaptor` でキャプチャした `Question` の `title="NewT"`, `content="NewC"`, `questioner="NewQ"` であることを確認する |

---

## 2. AnswerService（サービス層）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 16 | `AnswerServiceTest` | `createAnswer_firstAnswer_setsQuestionStatusToAnswered` | 未削除質問（id=1）が `UNANSWERED` で存在し、有効回答数が 0 件 | 有効な `AnswerRequest` で `createAnswer(1L, req)` を呼ぶ | 回答が保存され、`ArgumentCaptor` でキャプチャした `Question` の `status` が `ANSWERED` であることを確認する |
| 17 | `AnswerServiceTest` | `createAnswer_secondAnswer_statusRemainsAnswered` | 未削除質問が `ANSWERED` で存在し、有効回答数が 1 件 | 有効な `AnswerRequest` で `createAnswer(1L, req)` を呼ぶ | 回答が保存される。戻り値の `AnswerResponse` が返り、質問の `status` 変更は発生しない（回答保存後の有効回答数が 2 件のため） |
| 18 | `AnswerServiceTest` | `createAnswer_questionNotFound_throwsResourceNotFoundException` | id=99 の質問が存在しない | `createAnswer(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 19 | `AnswerServiceTest` | `createAnswer_maxAnswers_throwsBusinessException` | 有効回答数が 3 件 | `createAnswer(1L, req)` を呼ぶ | `BusinessException` がスローされる（メッセージが「回答は最大3件までです。」） |
| 20 | `AnswerServiceTest` | `updateAnswer_valid_returnsUpdated` | 未削除回答（id=1）と紐づく未削除質問が存在する | `content="New"`, `responder="Resp"` の `AnswerRequest` で `updateAnswer(1L, req)` を呼ぶ | 更新後の `AnswerResponse` が返り、`content="New"`, `responder="Resp"` であることを確認する |
| 21 | `AnswerServiceTest` | `updateAnswer_answerNotFound_throwsResourceNotFoundException` | id=99 の回答が存在しない | `updateAnswer(99L, req)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 22 | `AnswerServiceTest` | `updateAnswer_whenParentQuestionSoftDeleted_throwsResourceNotFoundException` | 未削除回答（id=1）は存在するが、紐づく質問が論理削除済み | `updateAnswer(1L, req)` を呼ぶ | 仕様：論理削除済み質問に紐づく回答は更新不可。`ResourceNotFoundException` がスローされる |
| 23 | `AnswerServiceTest` | `deleteAnswer_lastAnswer_setsQuestionStatusToUnanswered` | 有効回答数が 1 件、質問が `ANSWERED` | `deleteAnswer(1L)` を呼ぶ | 回答の `deletedAt` が null でなく、`ArgumentCaptor` でキャプチャした `Question` の `status` が `UNANSWERED` であることを確認する |
| 24 | `AnswerServiceTest` | `deleteAnswer_notLastAnswer_statusRemainsAnswered` | 有効回答数が 2 件、質問が `ANSWERED` | `deleteAnswer(1L)` を呼ぶ | 回答の `deletedAt` が null でなくなる。削除後有効回答数が 1 件のため、質問の `status` 変更は発生しない |
| 25 | `AnswerServiceTest` | `deleteAnswer_answerNotFound_throwsResourceNotFoundException` | id=99 の回答が存在しない | `deleteAnswer(99L)` を呼ぶ | `ResourceNotFoundException` がスローされる |
| 57 | `AnswerServiceTest` | `deleteAnswer_lastAnswer_verifiesQuestionSavedWithUnanswered` | 有効回答数が 1 件、質問が `ANSWERED` | `deleteAnswer(1L)` を呼ぶ | `questionRepository.save` が呼ばれ、キャプチャした引数の `status` が `UNANSWERED` であることを確認する（#23 の副作用確認補強） |

---

## 3. QuestionController（コントローラー層 / MockMvc）

> 期待結果には確認すべき主要フィールドを具体的に記載する。

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 26 | `QuestionControllerTest` | `getQuestions_returns200` | `QuestionService.getQuestions` が 2 件のリストを返す | `GET /api/questions` | HTTP 200、JSON 配列（2 要素）が返る |
| 27 | `QuestionControllerTest` | `getQuestion_exists_returns200` | `QuestionService.getQuestion(1L)` が `id=1`, `title="Test"`, `status="UNANSWERED"`, `answers=[]` の `QuestionDetailResponse` を返す | `GET /api/questions/1` | HTTP 200、レスポンス JSON の `$.id=1`, `$.title="Test"`, `$.status="UNANSWERED"`, `$.answers` が配列であることを確認する |
| 28 | `QuestionControllerTest` | `getQuestion_notExists_returns404` | `QuestionService.getQuestion(99L)` が `ResourceNotFoundException` をスロー | `GET /api/questions/99` | HTTP 404、レスポンス JSON の `$.status=404`, `$.error="Not Found"` を確認する |
| 29 | `QuestionControllerTest` | `createQuestion_valid_returns201` | `QuestionService.createQuestion` が `id=1`, `title="T"`, `status="UNANSWERED"` の `QuestionResponse` を返す | 有効な JSON ボディで `POST /api/questions` | HTTP 201、レスポンス JSON の `$.id=1`, `$.title="T"`, `$.status="UNANSWERED"` を確認する |
| 30 | `QuestionControllerTest` | `createQuestion_missingTitle_returns400WithFieldError` | なし | `title` が空文字の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='title')]` が存在し `message` が空でないことを確認する |
| 31 | `QuestionControllerTest` | `createQuestion_emptyContent_returns400WithFieldError` | なし | `content` が null の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='content')]` が存在することを確認する |
| 32 | `QuestionControllerTest` | `createQuestion_titleTooLong_returns400WithFieldError` | なし | `title` が 101 文字の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='title')]` が存在することを確認する |
| 33 | `QuestionControllerTest` | `updateQuestion_valid_returns200` | `QuestionService.updateQuestion` が `id=1`, `title="NewT"` の `QuestionResponse` を返す | 有効な JSON ボディで `PUT /api/questions/1` | HTTP 200、レスポンス JSON の `$.id=1`, `$.title="NewT"` を確認する |
| 34 | `QuestionControllerTest` | `updateQuestion_notExists_returns404` | `QuestionService.updateQuestion` が `ResourceNotFoundException` をスロー | `PUT /api/questions/99` | HTTP 404、`$.status=404`, `$.error="Not Found"` を確認する |
| 35 | `QuestionControllerTest` | `deleteQuestion_exists_returns200` | `QuestionService.deleteQuestion` が正常終了する | `DELETE /api/questions/1` | HTTP 200、`$.message="質問を削除しました。"` を確認する |
| 36 | `QuestionControllerTest` | `deleteQuestion_notExists_returns404` | `QuestionService.deleteQuestion` が `ResourceNotFoundException` をスロー | `DELETE /api/questions/99` | HTTP 404、`$.status=404`, `$.error="Not Found"` を確認する |
| 58 | `QuestionControllerTest` | `createQuestion_missingQuestioner_returns400WithFieldError` | なし | `questioner` が null の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='questioner')]` が存在することを確認する |
| 59 | `QuestionControllerTest` | `createQuestion_blankTitle_returns400WithFieldError` | なし | `title` が `"   "` （空白のみ）の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='title')]` が存在することを確認する（`@NotBlank` は空白のみも拒否） |
| 60 | `QuestionControllerTest` | `createQuestion_blankContent_returns400WithFieldError` | なし | `content` が `"   "` （空白のみ）の JSON ボディで `POST /api/questions` | HTTP 400、`$.fieldErrors[?(@.field=='content')]` が存在することを確認する |
| 61 | `QuestionControllerTest` | `updateQuestion_invalidInput_returns400WithFieldError` | なし | `title` が空文字の JSON ボディで `PUT /api/questions/1` | HTTP 400、`$.fieldErrors[?(@.field=='title')]` が存在することを確認する（更新系にもバリデーションが効くことを確認） |

---

## 4. AnswerController（コントローラー層 / MockMvc）

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 37 | `AnswerControllerTest` | `createAnswer_valid_returns201` | `AnswerService.createAnswer` が `id=1`, `questionId=1`, `content="Ans"` の `AnswerResponse` を返す | 有効な JSON ボディで `POST /api/questions/1/answers` | HTTP 201、レスポンス JSON の `$.id=1`, `$.questionId=1`, `$.content="Ans"` を確認する |
| 38 | `AnswerControllerTest` | `createAnswer_missingContent_returns400WithFieldError` | なし | `content` が空文字の JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`$.fieldErrors[?(@.field=='content')]` が存在することを確認する |
| 39 | `AnswerControllerTest` | `createAnswer_questionNotFound_returns404` | `AnswerService.createAnswer` が `ResourceNotFoundException` をスロー | 有効な JSON ボディで `POST /api/questions/99/answers` | HTTP 404、`$.status=404`, `$.error="Not Found"` を確認する |
| 40 | `AnswerControllerTest` | `createAnswer_maxAnswers_returns400` | `AnswerService.createAnswer` が `BusinessException("回答は最大3件までです。")` をスロー | 有効な JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`$.message="回答は最大3件までです。"` を確認する |
| 41 | `AnswerControllerTest` | `updateAnswer_valid_returns200` | `AnswerService.updateAnswer` が `id=1`, `content="Updated"` の `AnswerResponse` を返す | 有効な JSON ボディで `PUT /api/answers/1` | HTTP 200、レスポンス JSON の `$.id=1`, `$.content="Updated"` を確認する |
| 42 | `AnswerControllerTest` | `updateAnswer_answerNotFound_returns404` | `AnswerService.updateAnswer` が `ResourceNotFoundException` をスロー | 有効な JSON ボディで `PUT /api/answers/99` | HTTP 404、`$.status=404`, `$.error="Not Found"` を確認する |
| 43 | `AnswerControllerTest` | `deleteAnswer_valid_returns200` | `AnswerService.deleteAnswer` が正常終了する | `DELETE /api/answers/1` | HTTP 200、`$.message="回答を削除しました。"` を確認する |
| 44 | `AnswerControllerTest` | `deleteAnswer_notFound_returns404` | `AnswerService.deleteAnswer` が `ResourceNotFoundException` をスロー | `DELETE /api/answers/99` | HTTP 404、`$.status=404`, `$.error="Not Found"` を確認する |
| 62 | `AnswerControllerTest` | `createAnswer_missingResponder_returns400WithFieldError` | なし | `responder` が null の JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`$.fieldErrors[?(@.field=='responder')]` が存在することを確認する |
| 63 | `AnswerControllerTest` | `createAnswer_blankContent_returns400WithFieldError` | なし | `content` が `"   "` （空白のみ）の JSON ボディで `POST /api/questions/1/answers` | HTTP 400、`$.fieldErrors[?(@.field=='content')]` が存在することを確認する（`@NotBlank` は空白のみも拒否） |
| 64 | `AnswerControllerTest` | `updateAnswer_invalidInput_returns400WithFieldError` | なし | `content` が空文字の JSON ボディで `PUT /api/answers/1` | HTTP 400、`$.fieldErrors[?(@.field=='content')]` が存在することを確認する（更新系にもバリデーションが効くことを確認） |

---

## 5. GlobalExceptionHandler（例外ハンドラー）

> 実装の `ErrorResponse` が持つフィールド：`status`, `error`, `message`, `timestamp`, `fieldErrors`。
> `timestamp` は `OffsetDateTime`（Asia/Tokyo）。
> `fieldErrors` はバリデーションエラー時のみ付与。

| # | テストクラス | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 45 | `GlobalExceptionHandlerTest` | `handleResourceNotFoundException_returns404` | なし | `ResourceNotFoundException("質問が見つかりません。id=99")` をハンドラーに渡す | HTTP 404、`status=404`, `error="Not Found"`, `message="質問が見つかりません。id=99"`, `timestamp` が null でないことを確認する |
| 46 | `GlobalExceptionHandlerTest` | `handleBusinessException_returns400` | なし | `BusinessException("回答は最大3件までです。")` をハンドラーに渡す | HTTP 400、`status=400`, `error="Bad Request"`, `message="回答は最大3件までです。"`, `timestamp` が null でないことを確認する |
| 47 | `GlobalExceptionHandlerTest` | `handleValidationException_returns400WithFieldErrors` | なし | `MethodArgumentNotValidException`（`title` フィールドのエラーあり）をハンドラーに渡す | HTTP 400、`status=400`, `error="Validation Failed"`, `message="入力内容に誤りがあります。"`, `fieldErrors[0].field="title"`, `fieldErrors[0].message` が空でないことを確認する |
| 48 | `GlobalExceptionHandlerTest` | `handleUnexpectedException_returns500` | なし | `RuntimeException("予期しないエラー")` をハンドラーに渡す | HTTP 500、`status=500`, `error="Internal Server Error"`, `message="予期しないエラーが発生しました。"`, `fieldErrors` が空リストであることを確認する |

---

## 6. フロントエンド API クライアント（Vitest）

### 6-1. apiFetch 基本動作（client.test.ts）

| # | テストファイル | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 49 | `client.test.ts` | `apiFetch_success_returnsData` | `fetch` モックが `200 OK` と `{"id":1}` を返す | `apiFetch<{id: number}>("/api/test")` を呼ぶ | `{ id: 1 }` が返る |
| 50 | `client.test.ts` | `apiFetch_error_throwsApiError` | `fetch` モックが `404` と `{"status":404,"error":"Not Found","message":"質問が見つかりません。","timestamp":"...","fieldErrors":[]}` を返す | `apiFetch("/api/questions/99")` を呼ぶ | `ApiError` がスローされ、`error.status === 404`, `error.message === "質問が見つかりません。"`, `error.error === "Not Found"` を確認する |
| 65 | `client.test.ts` | `apiFetch_networkError_throwsOriginalError` | `fetch` モックが `TypeError: Failed to fetch` をスロー | `apiFetch("/api/test")` を呼ぶ | `TypeError` がそのままスローされる（`ApiError` ではなく fetch が投げた例外が伝播する） |

### 6-2. API ラッパー関数（questions.test.ts / answers.test.ts）

| # | テストファイル | テストメソッド | 事前条件 | 操作 | 期待結果 |
|---|------------|--------------|---------|------|---------|
| 66 | `questions.test.ts` | `getQuestions_withAllParams_buildsCorrectUrl` | `fetch` モックが `200 OK` と `[]` を返す | `getQuestions({ keyword: "Spring", status: "UNANSWERED", sort: "asc" })` を呼ぶ | `fetch` が `/api/questions?keyword=Spring&status=UNANSWERED&sort=asc` を含む URL で呼ばれることを確認する |
| 67 | `questions.test.ts` | `getQuestions_noParams_callsBaseUrl` | `fetch` モックが `200 OK` と `[]` を返す | `getQuestions()` を呼ぶ | `fetch` が `/api/questions` で呼ばれ、クエリ文字列が付与されないことを確認する |
| 68 | `questions.test.ts` | `createQuestion_sendsPostWithJsonBody` | `fetch` モックが `201` と `{"id":1,...}` を返す | `createQuestion({ title:"T", content:"C", questioner:"Q" })` を呼ぶ | `fetch` が `method: "POST"`, `body: '{"title":"T","content":"C","questioner":"Q"}'`, `Content-Type: application/json` で呼ばれることを確認する |
| 69 | `answers.test.ts` | `deleteAnswer_sendsDeleteMethod` | `fetch` モックが `200 OK` と `{"message":"..."}` を返す | `deleteAnswer(1)` を呼ぶ | `fetch` が `/api/answers/1` に対して `method: "DELETE"` で呼ばれることを確認する |

---

## 人間が実施するテスト

> 自動テストでは判定が困難なもの、または実機・目視での確認が適切なものを分離。

| # | カテゴリ | 確認内容 | 実施タイミング |
|---|---------|---------|-------------|
| H-1 | 統合テスト（DB） | `deleteQuestion` で質問と関連回答が同一トランザクション内で論理削除されること。途中でエラーが起きた場合にロールバックされることを、実 MySQL コンテナを使った統合テストで確認する | 実 DB 環境構築後 |
| H-2 | 統合テスト（DB） | `created_at` / `updated_at` が `Asia/Tokyo` タイムゾーンで DB に正しく保存されること。`SHOW VARIABLES LIKE 'time_zone'` や SELECT 結果の目視確認で検証する | Docker MySQL 起動後 |
| H-3 | UIテスト（ブラウザ） | バリデーションエラー時にフロントエンド画面でフィールド単位のエラーメッセージが表示され、どのフィールドのエラーかが利用者に伝わること（P-02 質問登録、P-06 質問修正、回答登録モーダルで確認） | フロントエンド実機動作確認時 |

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

    // ArgumentCaptor の使い方例
    @Test
    void createQuestion_capturesEntityWithCorrectFields() {
        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        when(questionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        QuestionRequest req = new QuestionRequest();
        req.setTitle("T"); req.setContent("C"); req.setQuestioner("Q");
        questionService.createQuestion(req);

        Question saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("T");
        assertThat(saved.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
        assertThat(saved.getDeletedAt()).isNull();
    }
}
```

**コントローラー層（例：QuestionController）**
```java
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean QuestionService questionService;
    @Autowired ObjectMapper objectMapper;

    // JSONPath による期待結果の具体化例
    @Test
    void getQuestion_exists_returns200() throws Exception {
        // ...
        mockMvc.perform(get("/api/questions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.status").value("UNANSWERED"))
            .andExpect(jsonPath("$.answers").isArray());
    }
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

test('apiFetch_networkError_throwsOriginalError', async () => {
  vi.mocked(fetch).mockRejectedValue(new TypeError('Failed to fetch'));
  await expect(apiFetch('/api/test')).rejects.toThrow(TypeError);
});
```

**API ラッパー関数（例：questions.test.ts）**
```typescript
test('getQuestions_withAllParams_buildsCorrectUrl', async () => {
  vi.mocked(fetch).mockResolvedValue(new Response('[]', { status: 200 }));
  await getQuestions({ keyword: 'Spring', status: 'UNANSWERED', sort: 'asc' });
  const url = vi.mocked(fetch).mock.calls[0][0] as string;
  expect(url).toContain('keyword=Spring');
  expect(url).toContain('status=UNANSWERED');
  expect(url).toContain('sort=asc');
});
```
