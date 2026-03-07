import { apiFetch } from './client';
import type { Answer, AnswerRequest } from '../types';

export function createAnswer(questionId: number, data: AnswerRequest): Promise<Answer> {
  return apiFetch<Answer>(`/api/questions/${questionId}/answers`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateAnswer(answerId: number, data: AnswerRequest): Promise<Answer> {
  return apiFetch<Answer>(`/api/answers/${answerId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

// バックエンド仕様確認: DELETE /api/answers/{answerId} は 200 OK でボディなしを返す想定
export function deleteAnswer(answerId: number): Promise<void> {
  return apiFetch<void>(`/api/answers/${answerId}`, { method: 'DELETE' });
}
