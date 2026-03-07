import { BrowserRouter, Route, Routes } from 'react-router-dom';
import TopPage from './pages/TopPage';
import QuestionNewPage from './pages/QuestionNewPage';
import QuestionEditPage from './pages/QuestionEditPage';
import UnansweredListPage from './pages/UnansweredListPage';
import QuestionDetailPage from './pages/QuestionDetailPage';
import QaListPage from './pages/QaListPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<TopPage />} />
        <Route path="/questions/new" element={<QuestionNewPage />} />
        <Route path="/questions/unanswered" element={<UnansweredListPage />} />
        <Route path="/questions/:id" element={<QuestionDetailPage />} />
        <Route path="/questions/:id/edit" element={<QuestionEditPage />} />
        <Route path="/qa" element={<QaListPage />} />
      </Routes>
    </BrowserRouter>
  );
}
