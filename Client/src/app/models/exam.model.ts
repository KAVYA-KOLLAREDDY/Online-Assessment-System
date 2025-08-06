import { Users } from "./student.model";

export interface Exam {
    examId: number; 
    title: string;
    description?: string;
    startTime: Date;
    endTime: Date;
    examStatus: 'SCHEDULED' | 'ONGOING' | 'COMPLETED';
    createdAt: Date;
    createdBy:Users;
  }
  