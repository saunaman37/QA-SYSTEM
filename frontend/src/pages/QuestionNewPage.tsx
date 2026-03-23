import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createQuestion } from '../api/questions';
import { ApiError } from '../api/client';
import ErrorMessage from '../components/ErrorMessage';
import AppHeader from '../components/AppHeader';

type FieldErrors = {
  title?: string;
  content?: string;
  questioner?: string;
};

function validate(title: string, content: string, questioner: string): FieldErrors {
  const errors: FieldErrors = {};
  if (!title.trim()) {
    errors.title = 'タイトルは必須です。';
  } else if (title.length > 100) {
    errors.title = 'タイトルは100文字以内で入力してください。';
  }
  if (!content.trim()) {
    errors.content = '質問内容は必須です。';
  } else if (content.length > 500) {
    errors.content = '質問内容は500文字以内で入力してください。';
  }
  if (!questioner.trim()) {
    errors.questioner = '質問者は必須です。';
  } else if (questioner.length > 50) {
    errors.questioner = '質問者は50文字以内で入力してください。';
  }
  return errors;
}

export default function QuestionNewPage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [questioner, setQuestioner] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [generalError, setGeneralError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError('');

    const errors = validate(title, content, questioner);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setSubmitting(true);
    try {
      await createQuestion({ title, content, questioner });
      navigate('/qa');
    } catch (err) {
      if (err instanceof ApiError && err.fieldErrors && err.fieldErrors.length > 0) {
        const fe: FieldErrors = {};
        for (const e of err.fieldErrors) {
          if (e.field === 'title' || e.field === 'content' || e.field === 'questioner') {
            fe[e.field] = e.message;
          }
        }
        setFieldErrors(fe);
      } else {
        setGeneralError('システムエラーが発生しました。しばらくしてから再度お試しください。');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <AppHeader title="質問登録" showBackButton={true} />
      <div style={{ maxWidth: '600px', margin: '2rem auto', padding: '0 var(--spacing-md)' }}>
        {generalError && <ErrorMessage message={generalError} />}
        <form onSubmit={handleSubmit}>
          <div style={{ marginTop: '1rem' }}>
            <label style={labelStyle}>タイトル<span style={requiredStyle}>*</span></label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              maxLength={100}
              style={inputStyle}
            />
            {fieldErrors.title && <ErrorMessage message={fieldErrors.title} />}
          </div>
          <div style={{ marginTop: '1rem' }}>
            <label style={labelStyle}>質問内容<span style={requiredStyle}>*</span></label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              maxLength={500}
              rows={6}
              style={inputStyle}
            />
            {fieldErrors.content && <ErrorMessage message={fieldErrors.content} />}
          </div>
          <div style={{ marginTop: '1rem' }}>
            <label style={labelStyle}>質問者<span style={requiredStyle}>*</span></label>
            <input
              type="text"
              value={questioner}
              onChange={(e) => setQuestioner(e.target.value)}
              maxLength={50}
              style={inputStyle}
            />
            {fieldErrors.questioner && <ErrorMessage message={fieldErrors.questioner} />}
          </div>
          <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => navigate('/')} style={secondaryBtnStyle}>キャンセル</button>
            <button type="submit" disabled={submitting} style={primaryBtnStyle}>登録する</button>
          </div>
        </form>
      </div>
    </div>
  );
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: '13px',
  color: 'var(--color-text-secondary)',
  marginBottom: '4px',
};

const requiredStyle: React.CSSProperties = {
  color: 'var(--color-danger-text)',
  marginLeft: '2px',
};

const inputStyle: React.CSSProperties = {
  display: 'block',
  width: '100%',
  background: 'var(--color-bg-secondary)',
  border: '0.5px solid var(--color-border)',
  borderRadius: 'var(--radius-md)',
  padding: '8px 10px',
  fontSize: '14px',
  boxSizing: 'border-box',
};

const primaryBtnStyle: React.CSSProperties = {
  background: 'var(--color-primary)',
  color: '#fff',
  border: 'none',
  borderRadius: 'var(--radius-md)',
  padding: '6px 16px',
  fontSize: '14px',
  cursor: 'pointer',
};

const secondaryBtnStyle: React.CSSProperties = {
  background: 'transparent',
  color: 'var(--color-text-secondary)',
  border: '0.5px solid var(--color-border)',
  borderRadius: 'var(--radius-md)',
  padding: '6px 16px',
  fontSize: '14px',
  cursor: 'pointer',
};
