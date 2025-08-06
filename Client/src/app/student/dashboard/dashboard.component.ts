import { Component, OnInit, inject } from '@angular/core';
import { ExamService } from '../../service/exam.service';
import { Chart, ChartConfiguration, ChartType } from 'chart.js';
import { CommonModule } from '@angular/common';
import { ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, BaseChartDirective, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: true
})
export class DashboardComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  private examService = inject(ExamService);

  // 📊 Dashboard Card Stats
  upcomingCount = 0;
  ongoingCount = 0;
  myExamCount = 0;
  certificationCount = 0;
  loading = true;

  // 📈 Donut Chart Variables
  donutChartLabels: string[] = [];
  donutChartData: number[] = [];
  donutChartColors: any[] = [];
  attemptedOngoingExams: any[] = [];
  selectedExamId: number | null = null;

  currentAttempt = 0;
  attemptMarks: number[] = [];

 

 donutChartConfig: ChartConfiguration<'doughnut'> = {
  type: 'doughnut',
  data: {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [],
      }
    ]
  },
  options: {
    responsive: true,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          generateLabels: () => {
            return [
              {
                text: 'Completed Exams (Passed/Failed)',
                fillStyle: '#28a745', // green
                strokeStyle: '#28a745',
                index: 0
              },
              {
                text: 'Ongoing Exams (Attempted)',
                fillStyle: '#ffc107', // yellow
                strokeStyle: '#ffc107',
                index: 1
              },
              {
                text: 'Not Yet Started',
                fillStyle: '#5038ED', // blue
                strokeStyle: '#5038ED',
                index: 2
              }
            ];
          }
        }
      },
      tooltip: {
        callbacks: {
          label: function (context) {
            return context.label ?? '';
          }
        }
      },
      datalabels: {
        color: '#000',
        font: {
          weight: 'bold'
        },
        formatter: function (_value, context) {
          const label = context.chart.data.labels?.[context.dataIndex];
          const exam = (context.chart as any).$context?.component?.chartExams?.[context.dataIndex];

          if (exam && typeof exam.attempts === 'number') {
            return exam.attempts.toString();
          }

          if (typeof label === 'string') {
            if (label.includes('Attempt')) {
              const match = label.match(/Attempt\s(\d+)/);
              return match ? match[1] : '';
            }
            if (label.includes('Not Started')) return '0';
            if (label.includes('Passed') || label.includes('Failed')) {
              const attemptMatch = label.match(/Attempt\s(\d+)/);
              return attemptMatch ? attemptMatch[1] : '3';
            }
          }
          return '';
        }
      }
    }
  }
};

  donutChartType: ChartType = 'doughnut';

  ngOnInit(): void {
    this.examService.getExams().subscribe({
      next: (exams: any[]) => {
        const now = new Date();
        console.log("Exams:",exams);
        // 💡 Count Stats
        this.upcomingCount = exams.filter(e => new Date(e.startTime) > now).length;
        this.ongoingCount = exams.filter(e => {
          const start = new Date(e.startTime);
          const end = new Date(e.endTime);
          return now >= start && now <= end;
        }).length;
        this.myExamCount = exams.filter(e => e.passed || (!e.passed && e.attempts >= 3)).length;
        this.attemptedOngoingExams = exams.filter(e => {
  const start = new Date(e.startTime);
  const end = new Date(e.endTime);
  const now = new Date();

  const isOngoing = now >= start && now <= end;
  const isCompleted = e.passed || (!e.passed && e.attempts >= 3);

  return (isOngoing && e.attempts > 0) || (isCompleted && e.attempts > 0);
});

if (this.attemptedOngoingExams.length > 0) {
  this.selectedExamId = this.attemptedOngoingExams[0].examId;
}
 // 🍩 Generate Donut Chart
        this.setupDropdownOptions(exams);

        this.formatDonutData(exams);
setTimeout(() => {
  this.chart?.update();  // ✅ force chart refresh
}, 0);
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Error loading exam data:', err);
        this.loading = false;
      }
    });

    this.fetchCertificates();
  }

  fetchCertificates(): void {
    this.examService.getStudentCertificates().subscribe({
      next: (data) => {
        this.certificationCount = data.length;
      },
      error: (err) => {
        console.error('❌ Error fetching certificates:', err);
      }
    });
  }

 
chartExams: any[] = []; // <- Add this in your component

private formatDonutData(exams: any[]): void {
  const now = new Date();
  const labels: string[] = [];
  const data: number[] = [];
  const colors: string[] = [];
  this.chartExams = [];

  exams.forEach(e => {
    const start = new Date(e.startTime);
    const end = new Date(e.endTime);
    const isInProgress = now >= start && now <= end;

    if (e.passed || (!e.passed && e.attempts >= 3)) {
      labels.push(`${e.title} - ${e.passed ? 'Passed' : 'Failed'} (Attempt ${e.attempts})`);
      data.push(1);
      colors.push('#28a745');
      this.chartExams.push(e);
    } else if (isInProgress && e.attempts > 0) {
      labels.push(`${e.title} - Attempt ${e.attempts} of 3`);
      data.push(1);
      colors.push('#ffc107');
      this.chartExams.push(e);
    } else if (isInProgress && e.attempts === 0) {
      labels.push(`${e.title} - Not Started`);
      data.push(1);
      colors.push('#5038ED');
      this.chartExams.push(e);
    }
  });

  this.donutChartConfig.data.labels = labels;
  this.donutChartConfig.data.datasets[0].data = data;
  this.donutChartConfig.data.datasets[0].backgroundColor = colors;
}
dropdownSelectedExamId: number | null = null;
dropdownOptions: any[] = [];
selectedExamDetails: any = null;

private setupDropdownOptions(exams: any[]): void {
  const now = new Date();
  this.dropdownOptions = exams
    .filter(e => {
      const start = new Date(e.startTime);
      return now < start || (now >= start && now <= new Date(e.endTime));
    })
    .map(e => ({
      label: `${e.title} - Attempt ${e.attempts}`,
      value: e.examId,
      details: e
    }));

  if (this.dropdownOptions.length > 0) {
    this.dropdownSelectedExamId = this.dropdownOptions[0].value;
    this.selectedExamDetails = this.dropdownOptions[0].details;
  }
}

onDropdownChange(): void {
  this.selectedExamDetails = this.dropdownOptions.find(opt => opt.value === this.dropdownSelectedExamId)?.details || null;
}

}
