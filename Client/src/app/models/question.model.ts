import { QuestionOption } from "./questionOption.model";

export interface Question {
    questionId: number;  // Maps to question_id
    examId: number;
    questionText: string;
    questionType: 'MCQ' | 'MSQ' | 'TrueFalse' | 'CodingSnippet';
    marks: number;
    options: QuestionOption[];  // Linking options
  }
  