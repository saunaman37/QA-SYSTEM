type Props = {
  status: 'UNANSWERED' | 'ANSWERED';
};

export default function StatusBadge({ status }: Props) {
  const isAnswered = status === 'ANSWERED';
  return (
    <span style={{
      display: 'inline-block',
      padding: '2px 8px',
      borderRadius: 'var(--radius-sm)',
      backgroundColor: isAnswered ? 'var(--color-answered)' : 'var(--color-unanswered)',
      color: isAnswered ? 'var(--color-answered-text)' : 'var(--color-unanswered-text)',
      fontSize: '12px',
      fontWeight: 500,
    }}>
      {isAnswered ? '回答済' : '未回答'}
    </span>
  );
}
