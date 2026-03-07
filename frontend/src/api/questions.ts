import { apiFetch } from './client';
import type { GetQuestionsParams, QuestionDetail, QuestionRequest, QuestionSummary } from '../types';

export function getQuestions(params: GetQuestionsParams = {}): Promise<QuestionSummary[]> {
  const query = new URLSearchParams();
  if (params.keyword !== undefined) query.set('keyword', params.keyword);
  if (params.status !== undefined) query.set('status', params.status);
  if (params.sort !== undefined) query.set('sort', params.sort);
  const qs = query.toString();
  return apiFetch<QuestionSummary[]>(`/api/questions${qs ? `?${qs}` : ''}`);
}

export function getQuestion(id: number): Promise<QuestionDetail> {
  return apiFetch<QuestionDetail>(`/api/questions/${id}`);
}

export function createQuestion(data: QuestionRequest): Promise<QuestionDetail> {
  return apiFetch<QuestionDetail>('/api/questions', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateQuestion(id: number, data: QuestionRequest): Promise<QuestionDetail> {
  return apiFetch<QuestionDetail>(`/api/questions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

// バックエンド仕様確認: DELETE /api/questions/{id} は 200 OK でボディなしを返す想定
export function deleteQuestion(id: number): Promise<void> {
  return apiFetch<void>(`/api/questions/${id}`, { method: 'DELETE' });
}
