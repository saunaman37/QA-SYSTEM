import { describe, test, expect, vi, afterEach } from 'vitest';
import { deleteAnswer } from '../api/answers';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('answers API', () => {
  // #69: deleteAnswer → DELETE /api/answers/{id} で呼ばれる
  test('deleteAnswer_sendsDeleteMethod', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ message: '回答を削除しました。' }), { status: 200 })
    );
    vi.stubGlobal('fetch', mockFetch);

    await deleteAnswer(1);

    const [calledUrl, calledInit] = mockFetch.mock.calls[0] as [string, RequestInit];
    expect(calledUrl).toContain('/api/answers/1');
    expect(calledInit.method).toBe('DELETE');
  });
});
