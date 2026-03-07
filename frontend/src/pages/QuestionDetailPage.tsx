import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { deleteQuestion, getQuestion } from '../api/questions';
import { deleteAnswer } from '../api/answers';
import { ApiError } from '../api/client';
import type { Answer, QuestionDetail } from '../types';
import ErrorMessage from '../components/ErrorMessage';
import DeleteConfirmDialog from '../components/DeleteConfirmDialog';
import AnswerFormModal from '../components/AnswerFormModal';
import AnswerEditModal from '../components/AnswerEditModal';

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const MM = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const HH = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}/${MM}/${dd} ${HH}:${mm}`;
}

type PageState = 'loading' | 'notFound' | 'error' | 'loaded';

export default function QuestionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const questionId = Number(id);

  const [pageState, setPageState] = useState<PageState>('loading');
  const [question, setQuestion] = useState<QuestionDetail | null>(null);
  const [pageError, setPageError] = useState('');

  // モーダル・ダイアログ状態
  const [showAnswerForm, setShowAnswerForm] = useState(false);
  const [editTarget, setEditTarget] = useState<Answer | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showQuestionDeleteDialog, setShowQuestionDeleteDialog] = useState(false);
  const [questionDeleting, setQuestionDeleting] = useState(false);
  const [deleteAnswerTarget, setDeleteAnswerTarget] = useState<Answer | null>(null);
  const [showAnswerDeleteDialog, setShowAnswerDeleteDialog] = useState(false);
  const [answerDeleting, setAnswerDeleting] = useState(false);

  async function fetchQuestion(isInitial: boolean) {
    try {
      const data = await getQuestion(questionId);
      setQuestion(data);
      if (isInitial) setPageState('loaded');
      setPageError('');
    } catch (err) {
      if (isInitial) {
        if (err instanceof ApiError && err.status === 404) {
          setPageState('notFound');
        } else {
          setPageState('error');
        }
      } else {
        setPageError('データの取得に失敗しました。しばらくしてから再度お試しください。');
      }
    }
  }

  useEffect(() => {
    fetchQuestion(true);
  }, [questionId]);

  async function handleQuestionDelete() {
    setQuestionDeleting(true);
    try {
      await deleteQuestion(questionId);
      navigate('/');
    } catch {
      setShowQuestionDeleteDialog(false);
      setPageError('削除に失敗しました。しばらくしてから再度お試しください。');
    } finally {
      setQuestionDeleting(false);
    }
  }

  async function handleAnswerDelete() {
    if (!deleteAnswerTarget) return;
    setAnswerDeleting(true);
    try {
      await deleteAnswer(deleteAnswerTarget.id);
      setShowAnswerDeleteDialog(false);
      setDeleteAnswerTarget(null);
      await fetchQuestion(false);
    } catch {
      setShowAnswerDeleteDialog(false);
      setPageError('削除に失敗しました。しばらくしてから再度お試しください。');
    } finally {
      setAnswerDeleting(false);
    }
  }

  if (pageState === 'loading') return <p>読み込み中...</p>;
  if (pageState === 'notFound') return <p>対象データが見つかりません。</p>;
  if (pageState === 'error') return <p>システムエラーが発生しました。しばらくしてから再度お試しください。</p>;
  if (!question) return null;

  const answerCount = question.answers.length;
  const atLimit = answerCount >= 3;

  return (
    <div style={{ maxWidth: '720px', margin: '2rem auto' }}>
      {pageError && <ErrorMessage message={pageError} />}

      {/* 質問エリア */}
      <div style={{ marginBottom: '1.5rem' }}>
        <h1>{question.title}</h1>
        <p style={{ whiteSpace: 'pre-wrap' }}>{question.content}</p>
        <p>質問者：{question.questioner}</p>
        <p>質問日時：{formatDate(question.createdAt)}</p>
        <p>
          ステータス：
          <span style={{
            display: 'inline-block',
            padding: '0.125rem 0.5rem',
            borderRadius: '4px',
            backgroundColor: question.status === 'ANSWERED' ? '#d4edda' : '#fff3cd',
            color: question.status === 'ANSWERED' ? '#155724' : '#856404',
          }}>
            {question.status === 'ANSWERED' ? '回答済' : '未回答'}
          </span>
        </p>
        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.75rem' }}>
          <button onClick={() => navigate(`/questions/${questionId}/edit`)}>質問を修正</button>
          <button onClick={() => setShowQuestionDeleteDialog(true)}>質問を削除</button>
        </div>
      </div>

      {/* 回答エリア */}
      <div>
        <h2>回答一覧</h2>
        {question.answers.length === 0 ? (
          <p>回答はまだありません。</p>
        ) : (
          question.answers.map((answer) => (
            <div key={answer.id} style={{ borderTop: '1px solid #eee', paddingTop: '0.75rem', marginTop: '0.75rem' }}>
              <p style={{ whiteSpace: 'pre-wrap' }}>{answer.content}</p>
              <p>回答者：{answer.responder}</p>
              <p>作成日時：{formatDate(answer.createdAt)}</p>
              <p>更新日時：{formatDate(answer.updatedAt)}</p>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                <button onClick={() => { setEditTarget(answer); setShowEditModal(true); }}>修正</button>
                <button onClick={() => { setDeleteAnswerTarget(answer); setShowAnswerDeleteDialog(true); }}>削除</button>
              </div>
            </div>
          ))
        )}
        <div style={{ marginTop: '1rem' }}>
          <button onClick={() => setShowAnswerForm(true)} disabled={atLimit}>
            回答を追加
          </button>
          {atLimit && <span style={{ marginLeft: '0.5rem', fontSize: '0.875rem', color: '#666' }}>回答は最大3件までです。</span>}
        </div>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <button onClick={() => navigate('/')}>戻る</button>
      </div>

      {/* モーダル・ダイアログ */}
      <AnswerFormModal
        isOpen={showAnswerForm}
        questionId={questionId}
        onSuccess={() => { setShowAnswerForm(false); fetchQuestion(false); }}
        onCancel={() => setShowAnswerForm(false)}
      />

      <AnswerEditModal
        isOpen={showEditModal}
        answer={editTarget}
        onSuccess={() => { setShowEditModal(false); setEditTarget(null); fetchQuestion(false); }}
        onCancel={() => { setShowEditModal(false); setEditTarget(null); }}
      />

      <DeleteConfirmDialog
        isOpen={showQuestionDeleteDialog}
        message="この質問を削除しますか？"
        onConfirm={handleQuestionDelete}
        onCancel={() => setShowQuestionDeleteDialog(false)}
        deleting={questionDeleting}
      />

      <DeleteConfirmDialog
        isOpen={showAnswerDeleteDialog}
        message="この回答を削除しますか？"
        onConfirm={handleAnswerDelete}
        onCancel={() => { setShowAnswerDeleteDialog(false); setDeleteAnswerTarget(null); }}
        deleting={answerDeleting}
      />
    </div>
  );
}
