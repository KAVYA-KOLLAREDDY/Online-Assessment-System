import {
  Component,
  OnInit,
  OnDestroy,
  HostListener,
  inject,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import prettier from 'prettier/standalone';
import parserBabel from 'prettier/parser-babel';
import { Question } from '../../models/question.model';
import { ExamService } from '../../service/exam.service';
import { AuthService } from '../../service/Auth.service';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';
import { FormsModule } from '@angular/forms';
import { MonacoEditorModule, NGX_MONACO_EDITOR_CONFIG } from 'ngx-monaco-editor';

enum QuestionStatus {
  UNCLICKED = 'UNCLICKED',
  ANSWERED = 'ANSWERED',
  SKIPPED = 'SKIPPED',
  REVIEW = 'REVIEW'
}

@Component({
  selector: 'app-exam-environment',
  standalone: true,
  imports: [CommonModule, MonacoEditorModule, FormsModule],
  templateUrl: './exam-environment.component.html',
  styleUrls: ['./exam-environment.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExamEnvironmentComponent implements OnInit, OnDestroy {
  questions: Question[] = [];
  currentQuestionIndex = 0;
  selectedAnswers: Record<number, number | number[]> = {};
  questionStatuses: Record<number, QuestionStatus> = {};
  private durationMinutes = 0;
  remainingSeconds = 0;
  timeUp = false;
  private timerSub: Subscription | null = null;
  private examSubmitted = false;
  private examService = inject(ExamService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private loggingService = inject(LoggingService);
  private cdr = inject(ChangeDetectorRef);

  modalVisible = false;
  modalTitle = '';
  modalMessage = '';
  modalType: 'violation' | 'confirmation' | 'error' = 'confirmation';
  modalCallback: () => void = () => {};

  attempts: number = 0;
  examStatusLoader: boolean = false;
  subtopicQuestionCounts: { [subtopic: string]: number } = {};
  showSubmitExamButton = false;

  // examTitle: string = ''; // 👈 hold exam title

  // editorOptions = {
  //   theme: 'vs-dark',
  //   language: 'plaintext',
  //   readOnly: true,
  //   automaticLayout: true,
  //   lineNumbers: 'on',
  //   minimap: { enabled: false }
  // };

  // codeToShow = '';
  // codeQuestionText = '';

  // splitQuestionAndCode(text: string): { question: string, code: string } {
  //   const m = text.match(/^(.*)'(.*)'$/);
  //   if (!m) return { question: text, code: '' };
  //   return {
  //     question: m[1].trim(),
  //     code: m[2].trim()
  //   };
  // }

  // detectLanguage(title: string): string {
  //   title = title.toLowerCase();
  //   if (title.includes('java')) return 'java';
  //   if (title.includes('python')) return 'python';
  //   if (title.includes('c++') || title.includes('cpp')) return 'cpp';
  //   if (title.includes('c ')) return 'c';
  //   if (title.includes('javascript') || title.includes('js')) return 'javascript';
  //   return 'plaintext';
  // }

  // updateCurrentCodingQuestion(): void {
  //   const rawText = this.currentQuestion.questionText;
  //   const { question, code } = this.splitQuestionAndCode(rawText);
  //   this.codeQuestionText = question;
  //   this.codeToShow = code;
  //   this.editorOptions = {
  //     ...this.editorOptions,
  //     language: this.detectLanguage(this.examTitle) // 👈 use stored title
  //   };
  // }
ngAfterViewChecked() {
  hljs.highlightAll();
}
  ngOnInit(): void {
  const voilatedButNotHandled = localStorage.getItem('voilationHandled') === 'false';
  if (voilatedButNotHandled) {
    this.showModal(
      'Violation Detected',
      'Your previous attempt was violated and cannot be resumed. You are being redirected to the exams list.',
      () => {
        localStorage.setItem('voilationHandled', 'true'); // Prevent loop
        this.router.navigate(['/student/exams']);
      },
      'violation'
    );
    return;
  }

  document.addEventListener('fullscreenchange', this.fullScreenViolate.bind(this));

  const examIdParam = this.route.snapshot.paramMap.get('id');
  const examId = Number(examIdParam);

  if (!examIdParam || isNaN(examId)) {
    this.showModal('Error', 'Invalid exam ID.', () => this.router.navigate(['/']), 'error');
    return;
  }

  this.examService.getExamById(examId).subscribe({
    next: (exam) => {
      console.log('Exam Response:', exam);
      // this.examTitle = exam.title;
      this.attempts = exam.attempts;

      // ✅ Prevent extra attempt if limit is reached
      if (this.attempts >= 3) {
        this.showModal(
          'Maximum Attempts Reached',
          'You have already completed all 3 allowed attempts for this exam. Redirecting...',
          () => {
            this.router.navigate(['/student/exams']);
          },
          'violation'
        );
        return;
      }

      // 🟡 Create new attempt safely
      this.examService.createAttempt(examId, this.attempts).subscribe({
        next: () => {
          localStorage.setItem("attemptStatus", "ONGOING");
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error("Create Attempt Error:", err);
          const errorMsg = err?.error || 'You have already attempted all the 3 attempts of this exam.';
          this.showModal('Attempt Blocked', errorMsg + ' Redirecting to exams list.', () => {
            this.router.navigate(['/student/exams']);
          }, 'violation');
        }
      });

      // ✅ Start timer only if allowed
      this.durationMinutes = exam.duration ?? 0;
      this.startTimer(this.durationMinutes * 60);
    },
    error: (err) => {
      console.error('Get Exam Error:', err);
      this.showModal('Error', 'Failed to load exam details.', () => this.router.navigate(['/']), 'error');
    },
  });

  this.examService.getQuestions(examId).subscribe({
    next: (response: any) => {
      console.log('Questions Response:', response);
      if (!response.questions || response.questions.length === 0) {
        this.showModal('Error', 'No questions available for this exam.', () => this.router.navigate(['/']), 'error');
        return;
      }
      this.questions = response.questions;
      this.subtopicQuestionCounts = response.subtopicQuestionCounts;
      this.questions.forEach(q => this.questionStatuses[q.questionId] = QuestionStatus.UNCLICKED);
//       if (this.questions.length && this.questions[0].questionType === 'CodingSnippet') {
//   this.updateCurrentCodingQuestion();
// }
      this.cdr.markForCheck();
    },
    error: (err) => {
      console.error('Get Questions Error:', err);
      this.showModal('Error', 'Failed to load questions: ' + (err.error || 'Unknown error'), () => this.router.navigate(['/']), 'error');
    },
  });

  this.preventBackNavigation();
}

  ngOnDestroy(): void {
    this.stopTimer();
    this.exitFullScreen();
    document.removeEventListener('fullscreenchange', this.fullScreenViolate.bind(this));
  }

  private startTimer(totalSec: number): void {
    this.remainingSeconds = totalSec;
    this.timerSub = interval(1000).subscribe(() => {
      if (this.remainingSeconds > 0) {
        this.remainingSeconds--;
        this.cdr.markForCheck();
      } else {
        this.stopTimer();
        this.timeUp = true;
        this.showModal('Time is up!', 'Your exam time is over. Submitting your exam.', () => {
          this.submitExam(false);
        }, 'violation');
      }
    });
  }

  private stopTimer(): void {
    if (this.timerSub) {
      this.timerSub.unsubscribe();
      this.timerSub = null;
    }
  }

  get displayTime(): string {
    const h = Math.floor(this.remainingSeconds / 3600);
    const m = Math.floor((this.remainingSeconds % 3600) / 60);
    const s = this.remainingSeconds % 60;
    return h > 0
      ? `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
      : `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  getStatusCounts() {
    return {
      attempted: Object.values(this.questionStatuses).filter(s => s === QuestionStatus.ANSWERED).length,
      nonAttempted: Object.values(this.questionStatuses).filter(s => s === QuestionStatus.UNCLICKED).length,
      review: Object.values(this.questionStatuses).filter(s => s === QuestionStatus.REVIEW).length,
      skipped: Object.values(this.questionStatuses).filter(s => s === QuestionStatus.SKIPPED).length
    };
  }

  saveAndNext(): void {
    const currentQuestion = this.questions[this.currentQuestionIndex];
    if (!(currentQuestion.questionId in this.selectedAnswers)) {
      this.showModal('No Answer Selected', 'Please select an answer before saving.', () => {}, 'error');
      return;
    }
    this.questionStatuses[currentQuestion.questionId] = QuestionStatus.ANSWERED;
    if (this.currentQuestionIndex === this.questions.length - 1) {
      this.showSubmitExamButton = true;
      this.cdr.markForCheck();
    } else {
      this.nextQuestion();
    }
  }

  saveForReview(): void {
    const currentQuestion = this.questions[this.currentQuestionIndex];
    this.questionStatuses[currentQuestion.questionId] = QuestionStatus.REVIEW;
    this.nextQuestion();
  }

  // clearResponse(): void {
  //   const currentQuestion = this.questions[this.currentQuestionIndex];
  //   delete this.selectedAnswers[currentQuestion.questionId];
  //   this.questionStatuses[currentQuestion.questionId] = QuestionStatus.UNCLICKED;
  //   this.cdr.markForCheck();
  // }

  skip(): void {
    const currentQuestion = this.questions[this.currentQuestionIndex];
    this.questionStatuses[currentQuestion.questionId] = QuestionStatus.SKIPPED;
    this.selectedAnswers[currentQuestion.questionId] = [];
    this.nextQuestion();
  }

  nextQuestion(): void {
  if (this.currentQuestionIndex < this.questions.length - 1) {
    this.currentQuestionIndex++;
    this.showSubmitExamButton = false;
    // const q = this.currentQuestion;
    // if (q?.questionType === 'CodingSnippet') {
    //   this.updateCurrentCodingQuestion();
    // }
    this.cdr.markForCheck();
  } else {
    this.triggerFinalSubmissionConfirmation();
  }
}

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.showSubmitExamButton = false;
    //   const q = this.currentQuestion;
    // if (q?.questionType === 'CodingSnippet') {
    //   this.updateCurrentCodingQuestion();
    // }
    this.cdr.markForCheck();
    }
  }

  goToQuestion(index: number): void {
    if (index >= 0 && index < this.questions.length) {
      this.currentQuestionIndex = index;
      this.showSubmitExamButton = false;
      this.cdr.markForCheck();
    }
  }
private triggerFinalSubmissionConfirmation(): void {
  const counts = this.getStatusCounts();

  let statusDetails: string[] = [];
  if (counts.nonAttempted > 0) statusDetails.push(`${counts.nonAttempted} unanswered`);
  if (counts.skipped > 0) statusDetails.push(`${counts.skipped} skipped`);
  if (counts.review > 0) statusDetails.push(`${counts.review} marked for review`);

  let message = `Are you sure you want to submit your exam?`;
  if (statusDetails.length > 0) {
    message += `\nYou have ${statusDetails.join(', ')} questions.`;
  }

  this.showModal('Confirm Exam Submission', message, () => {
    this.doubleConfirmExamSubmission();
  }, 'confirmation');
}

submitExamWithConfirmation(): void {
  this.triggerFinalSubmissionConfirmation();
}

private doubleConfirmExamSubmission(): void {
  this.showModal(
    'Final Confirmation !!',
    'This action cannot be undone. Do you really want to submit your exam?',
    () => {
      this.stopTimer();
      this.submitExam(true);
    },
    'confirmation'
  );
}


  isOptionSelected(questionId: number, optionId: number): boolean {
    const sel = this.selectedAnswers[questionId];
    return Array.isArray(sel) ? sel.includes(optionId) : sel === optionId;
  }

  selectAnswer(questionId: number, optionId: number): void {
    const q = this.questions.find((x) => x.questionId === questionId);
    if (!q) return;
    const current = this.selectedAnswers[questionId];
    if (q.questionType === 'MSQ') {
      const correctCount = q.options.filter((o) => o.isCorrect).length;
      let arr = Array.isArray(current) ? [...current] : [];
      if (arr.includes(optionId)) {
        arr = arr.filter((id) => id !== optionId);
      } else if (arr.length < correctCount) {
        arr.push(optionId);
      }
      this.selectedAnswers[questionId] = arr;
    } else {
      this.selectedAnswers[questionId] = optionId;
    }
    console.log(`SELECTED ANSWER - TRACK : `);
    console.log(this.selectedAnswers);
    this.cdr.markForCheck();
  }

  splitQuestionAndCode(text: string): {
  question: string;
  code: string | null;
  formattedCode: string | null;
} {
  const m = text.match(/^(.*)'(.*)'/);
  if (!m) {
    return { question: text, code: null, formattedCode: null };
  }

  const question = m[1].trim();
  const rawCode = m[2].trim();

  // Use highlight.js to auto-detect and highlight the raw code
  const result = hljs.highlightAuto(rawCode);

  return {
    question,
    code: rawCode,
    formattedCode: result.value // this is HTML-formatted code
  };
}

  private detectLanguage(code: string): string | null {
    const result = hljs.highlightAuto(code);
    return result.language || null;
  }

  private submitExam(successfullExamStatus: boolean, attemptStatus: string = "COMPLETED"): void {
    if (this.examSubmitted) return;
    this.examSubmitted = true;
    this.examStatusLoader = true;
    this.cdr.markForCheck();
    if (this.attempts >= 3) {
    this.examStatusLoader = false;
    this.showModal(
      'Maximum Attempts Reached',
      'You have already used all 3 allowed attempts for this exam. Your submission is blocked.',
      () => {
        this.router.navigate(['/student/exams']);
      },
      'violation'
    );
    return;
  }
    const examId = Number(this.route.snapshot.paramMap.get('id'));
    const user = this.authService.currentUser();
    if (!user || !examId) {
      this.examStatusLoader = false;
      this.showModal('Error', 'Unable to submit exam: Invalid user or exam ID.', () => this.router.navigate(['/']), 'error');
      return;
    }

    let payload;
    console.log("=====================RESULT========================");
    console.log(this.selectedAnswers);
    if (successfullExamStatus) {
      payload = {
        examId,
        responses: Object.entries(this.selectedAnswers).map(([qId, sel]) => ({
          question: +qId,
          selectedOption: Array.isArray(sel) ? sel : [sel],
        })),
        attemptStatus: "COMPLETED"
      };
    } else {
      console.log("UNSUCCESSFUL EXAM STATUS!");
      let responses = Object.entries(this.selectedAnswers).map(([qId, sel]) => ({
        question: +qId,
        selectedOption: Array.isArray(sel) ? sel : [sel],
      }));

      const includedQuestionIds = new Set(responses.map(r => r.question));
      for (let i = 0; i < this.questions.length; i++) {
        const questionId = this.questions[i].questionId;
        if (!includedQuestionIds.has(questionId)) {
          responses.push({
            question: questionId,
            selectedOption: [],
          });
        }
      }

      payload = {
        examId,
        responses,
        attemptStatus
      };
      console.log("PAYLOAD : ");
      console.log(payload);
    }

    this.examService.submitResponses(payload).subscribe(
      handleResposne(
        this.loggingService,
        (res) => {
          this.examStatusLoader = false;
          if (res.resultId !== undefined) {
              localStorage.removeItem('voilationHandled');  // ✅ Clear flag on successful exam completion
            this.router.navigate([`/student/results/${res.resultId}`], {
              replaceUrl: true,
            });
          }
          this.cdr.markForCheck();
        },
        () => {
          this.examStatusLoader = false;
          this.cdr.markForCheck();
        }
      )
    );
  }

private autoSubmitDueToViolation(reason: string): void {
  if (this.attempts >= 3) {
  this.examStatusLoader = false;
  this.showModal(
    'Maximum Attempts Reached',
    'You have already submitted all 3 attempts. This attempt will not be recorded.',
    () => this.router.navigate(['/student/exams']),
    'violation'
  );
  return;
}
    if (this.examSubmitted) return;
    this.examSubmitted = true;
    this.examStatusLoader = true;
    this.cdr.markForCheck();

    const examId = Number(this.route.snapshot.paramMap.get('id'));
    const user = this.authService.currentUser();
    if (!user || !examId) {
      this.examStatusLoader = false;
      this.showModal('Error', 'Unable to submit exam: Invalid user or exam.', () => this.router.navigate(['/']), 'error');
      return;
    }

    let responses = Object.entries(this.selectedAnswers).map(([qId, sel]) => ({
      question: +qId,
      selectedOption: Array.isArray(sel) ? sel : [sel],
    }));

    const includedQuestionIds = new Set(responses.map(r => r.question));
    for (let i = 0; i < this.questions.length; i++) {
      const questionId = this.questions[i].questionId;
      if (!includedQuestionIds.has(questionId)) {
        responses.push({
          question: questionId,
          selectedOption: [],
        });
      }
    }

    const payload = {
      examId,
      responses,
      attemptStatus: 'VOILATED',
    };

    localStorage.setItem('voilationHandled', 'false'); // 👈 Important part

    console.log('Submitting payload:', JSON.stringify(payload));

    this.examService.submitResponses(payload).subscribe({
      next: (res) => {
        console.log('Submission response:', res);
        this.examStatusLoader = false;
        this.showModal('Violation Detected !!', `${reason}. Your exam has been submitted.`, () => {
                  localStorage.setItem('voilationHandled', 'true'); // 👈 Mark it handled only after modal shown

          if (res.resultId !== undefined) {
            console.log('Navigating to result page with resultId:', res.resultId);
            this.router.navigate([`/student/results/${res.resultId}`], {
              replaceUrl: true,
            });
          } else {
            console.log('No resultId in response, redirecting to home');
            this.showModal('Error', 'No result ID returned. Redirecting to home page.', () => this.router.navigate(['/']), 'error');
          }
        }, 'violation');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Submission error:', err);
        this.examStatusLoader = false;
        const errorMessage = err?.error?.message || err?.message || 'Unknown error occurred.';
        this.showModal('Error', `Failed to submit exam: ${errorMessage}`, () => this.router.navigate(['/']), 'error');
        this.cdr.markForCheck();
      },
      complete: () => {
        console.log('Submission request completed');
      }
    });

    this.stopTimer();
  }

  private preventBackNavigation(): void {
    history.pushState(null, '', location.href);
    window.onpopstate = () => {
      if (!this.examSubmitted) {
        history.pushState(null, '', location.href);
        this.autoSubmitDueToViolation('Back button Pressed');
      }
    };
  }

  private exitFullScreen(): void {
    if (document.fullscreenElement) {
      document.exitFullscreen().catch((err) => {
        console.warn('Could not exit fullscreen:', err);
      });
    }
  }

  private fullScreenViolate(): void {
    if (!document.fullscreenElement) {
      document.exitFullscreen().catch((err) => {
        this.autoSubmitDueToViolation('Out from full screen!');
      });
    }
  }

  showModal(title: string, message: string, callback: () => void, modalType: 'violation' | 'confirmation' | 'error' = 'confirmation'): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.modalType = modalType;
    this.modalVisible = true;
    this.modalCallback = callback;
    this.cdr.markForCheck();
  }

  closeModalAndSubmit(): void {
    this.modalVisible = false;
    this.modalCallback();
    this.cdr.markForCheck();
  }

  closeModal(): void {
    this.modalVisible = false;
    this.cdr.markForCheck();
  }

  getBadgeClass(questionId: number): string {
    switch (this.questionStatuses[questionId]) {
      case QuestionStatus.ANSWERED:
        return 'bg-success';
      case QuestionStatus.SKIPPED:
        return 'bg-danger';
      case QuestionStatus.REVIEW:
        return 'bg-warning';
      case QuestionStatus.UNCLICKED:
        return 'bg-purple';
      default:
        return 'bg-secondary';
    }
  }

  trackByQuestion(index: number, question: Question): number {
    return question.questionId;
  }

  get currentQuestion(): Question {
    return this.questions[this.currentQuestionIndex];
  }

  get requiredMsqCount(): number {
    return this.currentQuestion.options.filter((o) => o.isCorrect).length;
  }

  get selectedArray(): number[] {
    const sel = this.selectedAnswers[this.currentQuestion.questionId];
    return Array.isArray(sel) ? sel : [];
  }

  @HostListener('window:beforeunload', ['$event'])
onBeforeUnload(event: BeforeUnloadEvent): void {
  if (!this.examSubmitted) {
    // Prevent default browser dialog
    event.preventDefault();
    // Trigger violation handling
    this.autoSubmitDueToViolation('Browser or system shutdown detected');
    // Some browsers may still show a dialog; setting returnValue is required for older browsers
    event.returnValue = '';
  }
}

@HostListener('document:keydown', ['$event'])
handleKeydown(event: KeyboardEvent): void {
  const key = event.key.toLowerCase();
  if (key === 'f5' || (event.ctrlKey && key === 'r')) {
    event.preventDefault();
    this.autoSubmitDueToViolation('Page reload attempted');
  } else if (event.altKey && event.key === 'F4') { // Detect Alt+F4
    event.preventDefault();
    this.autoSubmitDueToViolation('Alt+F4 key pressed');
  } else if (event.code === 'Escape') {
    event.preventDefault();
    this.autoSubmitDueToViolation('Escape key pressed');
  } else if (event.ctrlKey || event.metaKey) {
    event.preventDefault();
  }
}

  @HostListener('document:contextmenu', ['$event'])
  onRightClick(event: MouseEvent): void {
    event.preventDefault();
  }

  @HostListener('window:blur')
  onWindowBlur(): void {
    if (!this.examSubmitted) {
      setTimeout(() => {
        if (!document.hasFocus()) {
          this.autoSubmitDueToViolation('Tab switch or Windows key Pressed');
        }
      }, 300);
    }
  }

  @HostListener('document:selectstart', ['$event'])
  onSelectStart(event: Event): void {
    event.preventDefault();
  }

}