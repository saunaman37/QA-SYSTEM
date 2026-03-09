import { describe, test, expect, vi, afterEach } from 'vitest';
import { getQuestions, createQuestion } from '../api/questions';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('questions API', () => {
  // #66: 全パラメータあり → 正しいクエリ文字列が組み立てられる
  test('getQuestions_withAllParams_buildsCorrectUrl', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([]), { status: 200 })
    );
    vi.stubGlobal('fetch', mockFetch);

    await getQuestions({ keyword: 'Spring', status: 'UNANSWERED', sort: 'asc' });

    const calledUrl = mockFetch.mock.calls[0][0] as string;
    expect(calledUrl).toContain('keyword=Spring');
    expect(calledUrl).toContain('status=UNANSWERED');
    expect(calledUrl).toContain('sort=asc');
  });

  // #67: パラメータなし → クエリ文字列なしで呼ばれる
  test('getQuestions_noParams_callsBaseUrl', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([]), { status: 200 })
    );
    vi.stubGlobal('fetch', mockFetch);

    await getQuestions();

    const calledUrl = mockFetch.mock.calls[0][0] as string;
    expect(calledUrl).toMatch(/\/api\/questions$/);
    expect(calledUrl).not.toContain('?');
  });

  // #68: createQuestion → POST + 正しい JSON ボディで呼ばれる
  test('createQuestion_sendsPostWithJsonBody', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 1, title: 'T', content: 'C', questioner: 'Q', status: 'UNANSWERED', createdAt: '', updatedAt: '', answers: [] }), { status: 201 })
    );
    vi.stubGlobal('fetch', mockFetch);

    await createQuestion({ title: 'T', content: 'C', questioner: 'Q' });

    const [calledUrl, calledInit] = mockFetch.mock.calls[0] as [string, RequestInit];
    expect(calledUrl).toContain('/api/questions');
    expect(calledInit.method).toBe('POST');
    expect(calledInit.body).toBe(JSON.stringify({ title: 'T', content: 'C', questioner: 'Q' }));
    const headers = calledInit.headers as Record<string, string>;
    expect(headers['Content-Type']).toBe('application/json');
  });
});
