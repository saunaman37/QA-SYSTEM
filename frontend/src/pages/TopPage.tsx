import { useNavigate } from 'react-router-dom';

export default function TopPage() {
  const navigate = useNavigate();

  return (
    <div style={{ textAlign: 'center', marginTop: '4rem' }}>
      <h1>QAシステム</h1>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', marginTop: '2rem' }}>
        <button onClick={() => navigate('/questions/new')}>質問登録</button>
        <button onClick={() => navigate('/questions/unanswered')}>回答登録</button>
        <button onClick={() => navigate('/qa')}>Q&amp;A一覧</button>
      </div>
    </div>
  );
}
