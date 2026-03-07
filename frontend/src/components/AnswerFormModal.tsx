import { useEffect, useState } from 'react';
import { createAnswer } from '../api/answers';
import { ApiError } from '../api/client';
import ErrorMessage from './ErrorMessage';

type Props = {
  isOpen: boolean;
  questionId: number;
  onSuccess: () => void;
  onCancel: () => void;
};

type FieldErrors = {
  content?: string;
  responder?: string;
};

export function validateAnswerForm(content: string, responder: string): FieldErrors {
  const errors: FieldErrors = {};
  if (!content.trim()) {
    errors.content = '回答内容は必須です。';
  } else if (content.length > 500) {
    errors.content = '回答内容は500文字以内で入力してください。';
  }
  if (!responder.trim()) {
    errors.responder = '回答者は必須です。';
  } else if (responder.length > 50) {
    errors.responder = '回答者は50文字以内で入力してください。';
  }
  return errors;
}

export default function AnswerFormModal({ isOpen, questionId, onSuccess, onCancel }: Props) {
  const [content, setContent] = useState('');
  const [responder, setResponder] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [generalError, setGeneralError] = useState('');

  useEffect(() => {
    if (isOpen) {
      setContent('');
      setResponder('');
      setFieldErrors({});
      setGeneralError('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const errors = validateAnswerForm(content, responder);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    setGeneralError('');
    try {
      await createAnswer(questionId, { content, responder });
      onSuccess();
    } catch (err) {
      if (err instanceof ApiError && err.fieldErrors && err.fieldErrors.length > 0) {
        const fe: FieldErrors = {};
        for (const e of err.fieldErrors) {
          if (e.field === 'content' || e.field === 'responder') {
            fe[e.field] = e.message;
          }
        }
        setFieldErrors(fe);
      } else if (err instanceof Error) {
        setGeneralError(err.message);
      } else {
        setGeneralError('エラーが発生しました。');
      }
    }
  }

  return (
    <div style={overlayStyle}>
      <div style={dialogStyle}>
        <h2>回答を登録</h2>
        {generalError && <ErrorMessage message={generalError} />}
        <form onSubmit={handleSubmit}>
          <div style={{ marginTop: '1rem' }}>
            <label>回答内容</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              maxLength={500}
              rows={5}
              style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
            />
            {fieldErrors.content && <ErrorMessage message={fieldErrors.content} />}
          </div>
          <div style={{ marginTop: '1rem' }}>
            <label>回答者</label>
            <input
              type="text"
              value={responder}
              onChange={(e) => setResponder(e.target.value)}
              maxLength={50}
              style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
            />
            {fieldErrors.responder && <ErrorMessage message={fieldErrors.responder} />}
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" onClick={onCancel}>キャンセル</button>
            <button type="submit">登録</button>
          </div>
        </form>
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
  minWidth: '360px',
  maxWidth: '520px',
  width: '100%',
};
