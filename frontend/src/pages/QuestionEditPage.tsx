import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getQuestion, updateQuestion } from '../api/questions';
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

type FetchState = 'loading' | 'notFound' | 'error' | 'success';

export default function QuestionEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [fetchState, setFetchState] = useState<FetchState>('loading');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [questioner, setQuestioner] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    setFetchState('loading');
    getQuestion(Number(id))
      .then((data) => {
        setTitle(data.title);
        setContent(data.content);
        setQuestioner(data.questioner);
        setFieldErrors({});
        setApiError('');
        setFetchState('success');
      })
      .catch((err) => {
        if (err instanceof ApiError && err.status === 404) {
          setFetchState('notFound');
        } else {
          setFetchState('error');
        }
      });
  }, [id]);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFieldErrors({});
    setApiError('');

    const errors = validate(title, content, questioner);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setSubmitting(true);
    try {
      await updateQuestion(Number(id), { title, content, questioner });
      navigate(`/questions/${id}`);
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
        setApiError('システムエラーが発生しました。しばらくしてから再度お試しください。');
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (fetchState === 'loading') {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <p>読み込み中...</p>
        <button type="button" onClick={() => navigate('/')}>戻る</button>
      </div>
    );
  }

  if (fetchState === 'notFound') {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <p>対象データが見つかりません。</p>
        <button type="button" onClick={() => navigate('/')}>戻る</button>
      </div>
    );
  }

  if (fetchState === 'error') {
    return (
      <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
        <p>システムエラーが発生しました。しばらくしてから再度お試しください。</p>
        <button type="button" onClick={() => navigate('/')}>戻る</button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '600px', margin: '2rem auto' }}>
      <h1>質問修正</h1>
      {apiError && <ErrorMessage message={apiError} />}
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
          <button type="submit" disabled={submitting}>更新</button>
        </div>
      </form>
    </div>
  );
}
