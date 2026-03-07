type Props = {
  isOpen: boolean;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  deleting?: boolean;
};

export default function DeleteConfirmDialog({ isOpen, message, onConfirm, onCancel, deleting = false }: Props) {
  if (!isOpen) return null;

  return (
    <div style={overlayStyle}>
      <div style={dialogStyle}>
        <p>{message}</p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
          <button onClick={onCancel} disabled={deleting}>キャンセル</button>
          <button onClick={onConfirm} disabled={deleting}>削除する</button>
        </div>
      </div>
    </div>
  );
}

const overlayStyle: React.CSSProperties = {
  position: 'fixed',
  inset: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.5)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  zIndex: 1000,
};

const dialogStyle: React.CSSProperties = {
  backgroundColor: '#fff',
  padding: '1.5rem',
  borderRadius: '4px',
  minWidth: '320px',
  maxWidth: '480px',
};
