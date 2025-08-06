export interface ExamResult {
student: any;
exam: any;
    resultId?: number;
    examId: number;
    totalMarks: number;
    obtainedMarks: number;
    percentage?: number; // Auto-calculated in DB
    passed?: boolean;    // Auto-calculated in DB
    completedAt: Date;
  }
  