import { describe, test, expect, vi, afterEach } from 'vitest';
import { apiFetch, ApiError } from '../api/client';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('apiFetch', () => {
  // #49: 200 OK → データが返る
  test('apiFetch_success_returnsData', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 1 }), { status: 200 })
    ));

    const result = await apiFetch<{ id: number }>('/api/test');

    expect(result).toEqual({ id: 1 });
  });

  // #50: 4xx/5xx エラー → ApiError がスローされる（status, error, message を確認）
  test('apiFetch_error_throwsApiError', async () => {
    const errorBody = {
      status: 404,
      error: 'Not Found',
      message: '質問が見つかりません。',
      timestamp: '2026-03-10T00:00:00+09:00',
      fieldErrors: [],
    };
    // mockImplementation で毎回新しい Response を生成してボディ二重読み取りを回避
    vi.stubGlobal('fetch', vi.fn().mockImplementation(() =>
      Promise.resolve(new Response(JSON.stringify(errorBody), { status: 404 }))
    ));

    let caught: ApiError | undefined;
    try {
      await apiFetch('/api/questions/99');
    } catch (e) {
      caught = e as ApiError;
    }

    expect(caught).toBeInstanceOf(ApiError);
    expect(caught?.status).toBe(404);
    expect(caught?.error).toBe('Not Found');
    expect(caught?.message).toBe('質問が見つかりません。');
  });

  // #65: ネットワークエラー → TypeError がそのまま伝播する
  test('apiFetch_networkError_throwsOriginalError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    await expect(apiFetch('/api/test')).rejects.toThrow(TypeError);
    await expect(apiFetch('/api/test')).rejects.toThrow('Failed to fetch');
  });
});
