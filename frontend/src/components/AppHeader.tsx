import { useNavigate } from 'react-router-dom';

type Props = {
  title?: string;
  showBackButton?: boolean;
};

export default function AppHeader({ title = 'QAシステム', showBackButton = false }: Props) {
  const navigate = useNavigate();

  return (
    <header style={{
      backgroundColor: 'var(--color-primary)',
      height: '48px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 var(--spacing-lg)',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
        <span style={{ color: '#fff', fontWeight: 'bold', fontSize: '16px' }}>QA</span>
        <span style={{ color: '#fff', fontSize: '15px' }}>{title}</span>
      </div>
      {showBackButton && (
        <button
          type="button"
          onClick={() => navigate('/')}
          style={{
            background: 'rgba(255,255,255,0.15)',
            color: '#fff',
            border: 'none',
            borderRadius: 'var(--radius-md)',
            padding: '5px 12px',
            fontSize: '13px',
            cursor: 'pointer',
          }}
        >
          TOPへ戻る
        </button>
      )}
    </header>
  );
}
