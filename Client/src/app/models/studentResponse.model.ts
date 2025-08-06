export interface StudentResponse {
  responseId?: number;  // Optional since it's auto-generated in the backend
  question: number;  
  selectedOption: number;  
  submittedAt?: string;  // Optional since it's auto-generated
}
