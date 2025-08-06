export interface QuestionOption {
    optionId: number;  // Maps to option_id
    questionId: number;
    optionText: string;
    isCorrect?: boolean;  // Optional because the student shouldn't see this
  }
  