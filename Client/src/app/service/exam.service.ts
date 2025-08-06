import {
  Injectable,
  computed,
  effect,
  signal,
  Signal,
  inject,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Exam } from '../models/exam.model';
import { Question } from '../models/question.model';
import { StudentResponse } from '../models/studentResponse.model';
import { ExamResult } from '../models/examResult.model';
import { Role, Status, Users } from '../models/student.model';
import { jwtDecode } from 'jwt-decode';
import { Observable, single } from 'rxjs';
import { CommonService } from './Common.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ExamService {
  private envUrl = environment.apiUrl;
  private commonService = inject(CommonService);

  examMode = signal<Boolean>(false);

  user = signal<Users | null>(null);
  currentExam = signal<Exam | null>(null);

  constructor(private http: HttpClient) {}

  setUser(user: Users) {
    this.user.set(user);
  }

  changeActiveStatus(userId: number, status: boolean) {
    return this.commonService.put(`${this.envUrl}/users/updateStatus`, {
      userId,
      active: status,
      locked: false,
    });
  }

  register(userData: any, role: string) {
    return this.commonService.post(
      `${this.envUrl}/auth/register?role=${role}`,
      userData
    );
  }

  getPendingUsers() {
    return this.commonService.get<any[]>(
      `${this.envUrl}/student/pending-users`
    );
  }

  approveUser(userId: number) {
    return this.commonService.put<any[]>(
      `${this.envUrl}/student/admin/approve/${userId}`,
      {}
    );
  }

  rejectUser(id: number) {
    return this.commonService.delete<any[]>(
      `${this.envUrl}/student/reject/${id}`
    );
  }

  getAllUsers() {
    return this.commonService.get<any[]>(`${this.envUrl}/users`);
  }

  createSubject(subjectData: any) {
    return this.commonService.post(`${this.envUrl}/subjects/create`, subjectData);
  }

  getSubjects() {
    return this.commonService.get<any[]>(`${this.envUrl}/subjects/all`);
  }

  getAllsubjectsWithSubtopics() {
    return this.commonService.get<any[]>(
      `${this.envUrl}/subjects/subjects-with-subtopics`
    );
  }

  getSubjectByExam(examId : number){
    return this.http.get<any[]>(`${this.envUrl}/subjects/exam/${examId}`);
  }

  getSubjectByExamWithSubtopics(examId : number){
    return this.http.get<any[]>(`${this.envUrl}/subjects/exam/${examId}/with-subtopics`)
  }
  
   // New method to check if an exam has distributions
  hasExamDistributions(examId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.envUrl}/questions/exam/${examId}/has-distributions`);
  }

  getExamDistribution(examId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.envUrl}/questions/exam/${examId}/distributions`);
  }

  updateDistribution(requestBody: any){
    return this.http.put(`${this.envUrl}/questions/update-distribution`, requestBody, {responseType: 'json'});
  }

  updateSubjectStatus(subjectId: number, status: string) {
    return this.http.put(`${this.envUrl}/subjects/${subjectId}/status?status=${status}`, {}, {responseType: 'text'});
  }

  generateQuestions(requestBody: any): Observable<any> {
  return this.http.post(`${this.envUrl}/questions/generate-questions`, requestBody);
  }

  getExams() {
    return this.commonService.get<any[]>(`${this.envUrl}/exams/all`);
  }

  getMyExams() {
  return this.commonService.get<any[]>(`${this.envUrl}/exams/my`);
}

  getExamById(id: number) {
    return this.commonService.get<any>(`${this.envUrl}/exams/${id}`);
  }

  createExam(exam: Exam) {
    return this.commonService.post<Exam[]>(`${this.envUrl}/exams`, exam);
  }

  updateExam(examId: number, exam: Exam) {
    return this.commonService.put<Exam>(`${this.envUrl}/update/${examId}`, exam);
  }

  deleteExam(id: number) {
    return this.commonService.delete<Exam[]>(`${this.envUrl}/exams/delete/${id}`);
  }

  getAllQuestions(examId: number) {
    return this.commonService.get<Question[]>(`${this.envUrl}/questions/exams/${examId}`);
  }
  
  getQuestions(examId: number) {
    return this.commonService.get<Question[]>(`${this.envUrl}/questions/exam/${examId}`);
  }

  setCurrentExam(exam: Exam) {
    this.currentExam.set(exam);
  }

  getStudentResponses(examId: number) {
    return this.commonService.get<StudentResponse[]>(
      `${this.envUrl}/responses/exam/${examId}`
    );
  }

  submitResponses(responses: any): Observable<any> {
    return this.commonService.post(`${this.envUrl}/responses`, responses);
  }

  getAllExamResults() {
    return this.commonService.get<ExamResult[]>(`${this.envUrl}/exam-results/all`);
  }

  getExamResult(studentId: number, examId: number) {
    return this.commonService.get<ExamResult>(
      `${this.envUrl}/exam-results/exam/${examId}/student/${studentId}`
    );
  }

 getAllAttemptsForExam(examId: number) {
  return this.commonService.get<any[]>(`${this.envUrl}/exam-results/responses/latest/${examId}`);
}

  getExamResultById(id: number) {
    return this.commonService.get<any>(`${this.envUrl}/responses/${id}`);
  }

  getDifficultyAnalysis(resultId: number) {
  return this.http.get<any[]>(`${this.envUrl}/exam-results/result/${resultId}/difficulty-analysis`);
}

getQuestionAnalysis(resultId: number) {
  return this.http.get<any[]>(`${this.envUrl}/exam-results/result/${resultId}/question-analysis`);
}


  createAttempt(examId : number, attempts: number){
    return this.commonService.post(`${this.envUrl}/attempts`,{examId, attempts: attempts + 1});
  }

  calculateExamResult(studentId: number, examId: number) {
    return this.commonService.post<ExamResult>(
      `${this.envUrl}/exam-results/calculate/${studentId}/${examId}`,
      {}
    );
  }

  uploadLatest(formData: FormData) {
    return this.commonService.post(`${this.envUrl}/questions/upload-latest`, formData);
  }

  createSubTopics(subTopic: any) {
    return this.commonService.post(`${this.envUrl}/subtopics`, subTopic);
  }

  getAllSubTopics() {
    return this.commonService.get<any[]>(`${this.envUrl}/subtopics`);
  }

  getAllSubtopicsBySubject(subjectId: number) {
    return this.commonService.get<any[]>(
      `${this.envUrl}/subtopics/subject/${subjectId}`
    );
  }

  getSubject(subjectId: number) {
    return this.commonService.get<any>(`${this.envUrl}/subjects/${subjectId}`);
  }

  getStudentCertificates() {
    return this.http.get<any[]>(`${this.envUrl}/certificate/my-certificates`);
}
downloadCertificate(certificateId: number) {
    const url = `${this.envUrl}/certificate/file/${certificateId}?mode=attachment`;
    return this.http.get(url, { responseType: 'blob' });
  }

  // ✅ 3. View certificate in new tab
  viewCertificate(certificateId: number): string {
    return `${this.envUrl}/certificate/file/${certificateId}?mode=inline`;
  }

}
