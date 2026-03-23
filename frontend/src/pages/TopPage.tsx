import { useNavigate } from 'react-router-dom';
import AppHeader from '../components/AppHeader';

const cards = [
  {
    label: '質問登録',
    description: '新しい質問を追加する',
    path: '/questions/new',
    iconBg: 'var(--color-primary-light)',
    icon: '✏️',
  },
  {
    label: '回答登録',
    description: '未回答の質問に回答する',
    path: '/questions/unanswered',
    iconBg: 'var(--color-unanswered)',
    icon: '💬',
  },
  {
    label: 'Q&A一覧',
    description: '全件を検索・閲覧する',
    path: '/qa',
    iconBg: 'var(--color-answered)',
    icon: '📋',
  },
];

export default function TopPage() {
  const navigate = useNavigate();

  return (
    <div>
      <AppHeader title="QAシステム" showBackButton={false} />
      <div style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 var(--spacing-md)' }}>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: 'var(--spacing-md)',
          marginTop: '2rem',
        }}>
          {cards.map((card) => (
            <button
              key={card.path}
              onClick={() => navigate(card.path)}
              style={{
                border: '0.5px solid var(--color-border)',
                borderRadius: 'var(--radius-lg)',
                padding: '20px 16px',
                background: '#fff',
                cursor: 'pointer',
                textAlign: 'center',
                transition: 'background 0.15s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--color-bg-secondary)')}
              onMouseLeave={(e) => (e.currentTarget.style.background = '#fff')}
            >
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: card.iconBg,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto var(--spacing-sm)',
                fontSize: '20px',
              }}>
                {card.icon}
              </div>
              <div style={{ fontWeight: 'bold', fontSize: '15px', marginBottom: '6px' }}>{card.label}</div>
              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>{card.description}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
