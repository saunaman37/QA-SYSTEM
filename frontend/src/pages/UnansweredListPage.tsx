import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getQuestions } from '../api/questions';
import type { QuestionSummary, SortOrder } from '../types';
import ErrorMessage from '../components/ErrorMessage';
import AppHeader from '../components/AppHeader';
import StatusBadge from '../components/StatusBadge';

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const MM = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const HH = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}/${MM}/${dd} ${HH}:${mm}`;
}

export default function UnansweredListPage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState<SortOrder>('desc');
  const [questions, setQuestions] = useState<QuestionSummary[] | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function fetchQuestions(kw: string, sortOrder: SortOrder) {
    setLoading(true);
    setError('');
    try {
      const trimmed = kw.trim();
      const result = await getQuestions({
        status: 'UNANSWERED',
        sort: sortOrder,
        ...(trimmed ? { keyword: trimmed } : {}),
      });
      setQuestions(result);
    } catch {
      setError('データの取得に失敗しました。しばらくしてから再度お試しください。');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchQuestions('', 'desc');
  }, []);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    fetchQuestions(keyword, sort);
  }

  return (
    <div>
      <AppHeader title="回答対象一覧" showBackButton={true} />
      <div style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 var(--spacing-md)' }}>
        {error && <ErrorMessage message={error} />}
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: '1rem', alignItems: 'center' }}>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="キーワード"
            style={{
              flex: 1,
              background: 'var(--color-bg-secondary)',
              border: '0.5px solid var(--color-border)',
              borderRadius: 'var(--radius-md)',
              padding: '8px 10px',
              fontSize: '14px',
            }}
          />
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value as SortOrder)}
            style={{
              background: 'var(--color-bg-secondary)',
              border: '0.5px solid var(--color-border)',
              borderRadius: 'var(--radius-md)',
              padding: '8px 10px',
              fontSize: '14px',
            }}
          >
            <option value="desc">新着順</option>
            <option value="asc">古い順</option>
          </select>
          <button
            type="submit"
            disabled={loading}
            style={{
              background: 'var(--color-primary)',
              color: '#fff',
              border: 'none',
              borderRadius: 'var(--radius-md)',
              padding: '6px 16px',
              fontSize: '14px',
              cursor: 'pointer',
            }}
          >
            検索
          </button>
        </form>
        <div style={{ marginTop: '1.5rem' }}>
          {questions !== null && questions.length === 0 ? (
            <p>未回答の質問はありません。</p>
          ) : questions !== null ? (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={thStyle}>タイトル</th>
                  <th style={thStyle}>質問者</th>
                  <th style={thStyle}>質問日時</th>
                  <th style={thStyle}>ステータス</th>
                </tr>
              </thead>
              <tbody>
                {questions.map((q) => (
                  <tr key={q.id}>
                    <td style={tdStyle}>
                      <span
                        onClick={() => navigate(`/questions/${q.id}`)}
                        style={{ color: 'var(--color-link)', cursor: 'pointer', textDecoration: 'underline' }}
                      >
                        {q.title}
                      </span>
                    </td>
                    <td style={tdStyle}>{q.questioner}</td>
                    <td style={tdStyle}>{formatDate(q.createdAt)}</td>
                    <td style={tdStyle}><StatusBadge status={q.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : null}
        </div>
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
  borderBottom: '0.5px solid var(--color-border)',
  padding: '0.5rem',
};
