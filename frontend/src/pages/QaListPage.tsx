import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getQuestions } from '../api/questions';
import type { QuestionSummary, SortOrder, Status } from '../types';
import ErrorMessage from '../components/ErrorMessage';

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const MM = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const HH = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}/${MM}/${dd} ${HH}:${mm}`;
}

function statusLabel(status: Status): string {
  return status === 'UNANSWERED' ? '未回答' : '回答済';
}

export default function QaListPage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<Status | ''>('');
  const [sort, setSort] = useState<SortOrder>('desc');
  const [questions, setQuestions] = useState<QuestionSummary[] | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function fetchQuestions(kw: string, st: Status | '', sortOrder: SortOrder) {
    setLoading(true);
    setError('');
    try {
      const trimmed = kw.trim();
      const result = await getQuestions({
        sort: sortOrder,
        ...(trimmed ? { keyword: trimmed } : {}),
        ...(st ? { status: st } : {}),
      });
      setQuestions(result);
    } catch {
      setError('データの取得に失敗しました。しばらくしてから再度お試しください。');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchQuestions('', '', 'desc');
  }, []);

  function handleSearch(e: { preventDefault(): void }) {
    e.preventDefault();
    fetchQuestions(keyword, status, sort);
  }

  return (
    <div style={{ maxWidth: '800px', margin: '2rem auto' }}>
      <h1>Q&A一覧</h1>
      {error && <ErrorMessage message={error} />}
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="キーワード"
        />
        <select value={status} onChange={(e) => setStatus(e.target.value as Status | '')}>
          <option value="">全件</option>
          <option value="UNANSWERED">未回答のみ</option>
          <option value="ANSWERED">回答済のみ</option>
        </select>
        <select value={sort} onChange={(e) => setSort(e.target.value as SortOrder)}>
          <option value="desc">新着順</option>
          <option value="asc">古い順</option>
        </select>
        <button type="submit" disabled={loading}>検索</button>
      </form>
      <div style={{ marginTop: '1.5rem' }}>
        {questions !== null && questions.length === 0 ? (
          <p>該当するQ&Aはありません。</p>
        ) : questions !== null ? (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={thStyle}>タイトル</th>
                <th style={thStyle}>質問者</th>
                <th style={thStyle}>ステータス</th>
                <th style={thStyle}>質問日時</th>
              </tr>
            </thead>
            <tbody>
              {questions.map((q) => (
                <tr key={q.id}>
                  <td style={tdStyle}>
                    <span
                      onClick={() => navigate(`/questions/${q.id}`)}
                      style={{ color: 'blue', cursor: 'pointer', textDecoration: 'underline' }}
                    >
                      {q.title}
                    </span>
                  </td>
                  <td style={tdStyle}>{q.questioner}</td>
                  <td style={tdStyle}>{statusLabel(q.status)}</td>
                  <td style={tdStyle}>{formatDate(q.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </div>
      <div style={{ marginTop: '1.5rem' }}>
        <button type="button" onClick={() => navigate('/')}>戻る</button>
      </div>
    </div>
  );
}

const thStyle: React.CSSProperties = {
  borderBottom: '1px solid #ccc',
  textAlign: 'left',
  padding: '0.5rem',
};

const tdStyle: React.CSSProperties = {
  borderBottom: '1px solid #eee',
  padding: '0.5rem',
};
