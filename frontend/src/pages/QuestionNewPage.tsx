import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createQuestion } from '../api/questions';
import { ApiError } from '../api/client';
import ErrorMessage from '../components/ErrorMessage';

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
    <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
      <h1>質問登録</h1>
      {generalError && <ErrorMessage message={generalError} />}
      <form onSubmit={handleSubmit}>
        <div style={{ marginTop: '1rem' }}>
          <label>タイトル</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={100}
            style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
          />
          {fieldErrors.title && <ErrorMessage message={fieldErrors.title} />}
        </div>
        <div style={{ marginTop: '1rem' }}>
          <label>質問内容</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            maxLength={500}
            rows={6}
            style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
          />
          {fieldErrors.content && <ErrorMessage message={fieldErrors.content} />}
        </div>
        <div style={{ marginTop: '1rem' }}>
          <label>質問者</label>
          <input
            type="text"
            value={questioner}
            onChange={(e) => setQuestioner(e.target.value)}
            maxLength={50}
            style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
          />
          {fieldErrors.questioner && <ErrorMessage message={fieldErrors.questioner} />}
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem' }}>
          <button type="button" onClick={() => navigate('/')}>戻る</button>
          <button type="submit" disabled={submitting}>登録</button>
        </div>
      </form>
    </div>
  );
}
