export type Status = 'UNANSWERED' | 'ANSWERED';

export type SortOrder = 'asc' | 'desc';

export interface Answer {
  id: number;
  questionId: number;
  content: string;
  responder: string;
  createdAt: string;
  updatedAt: string;
}

export interface QuestionSummary {
  id: number;
  title: string;
  content: string;
  questioner: string;
  status: Status;
  createdAt: string;
  updatedAt: string;
}

export interface QuestionDetail {
  id: number;
  title: string;
  content: string;
  questioner: string;
  status: Status;
  createdAt: string;
  updatedAt: string;
  answers: Answer[];
}

export interface QuestionRequest {
  title: string;
  content: string;
  questioner: string;
}

export interface AnswerRequest {
  content: string;
  responder: string;
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  fieldErrors?: FieldError[];
}

export interface GetQuestionsParams {
  keyword?: string;
  status?: Status;
  sort?: SortOrder;
}
