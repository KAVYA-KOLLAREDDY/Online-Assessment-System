import { Component, inject, input, signal } from '@angular/core';
import { CommonModule, LocationStrategy, NgClass, NgIf } from '@angular/common';
import { ExamService } from '../../service/exam.service';
import { Chart, ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import confetti from 'canvas-confetti';
import { RouterLink } from '@angular/router';

Chart.register(ChartDataLabels);

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [NgIf, NgClass, BaseChartDirective, CommonModule, RouterLink],
  templateUrl: './result.component.html',
  styleUrl: './result.component.css'
})
export class ResultComponent {
  id = input.required<number>();
  private examService = inject(ExamService);
  result = signal<any | null>(null);
// Top of component
showCelebrationModal = signal(false);
  improvementTopics: { topic: string; percentage: number }[] = [];
  private location = inject(LocationStrategy);

  performanceCategoryMap: { [category: string]: string[] } = {
    'Best Performance': [],
    'Midlevel Performance': [],
    'Miserable Performance': []
  };
  chartPlugins = [ChartDataLabels];

  chartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#FF6F61', '#88B04B', '#FFA07A', '#20B2AA', '#9370DB'],
    }],
  };

  chartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      datalabels: {
        color: '#000',
        anchor: 'center',
        align: 'center',
        display: 'auto',
        formatter: (value: any) => Math.ceil(value) + '%',
        font: { weight: 'bold', size: 10 }
      },
      legend: {
        position: 'bottom',
        display: true,
        labels: {
          generateLabels: (chart) => {
            const data = chart.data;
            const bgColors = data.datasets[0].backgroundColor;
            const topics = this.result()?.topics || [];

            return data.labels?.map((label, i) => {
              const topic = topics.find((t: { topic: unknown }) => t.topic === label);
              const countLabel = topic ? `${topic.correctQuestions}/${topic.totalQuestions}` : '0/0';
              return {
                text: `${label}: ${topic ? Math.ceil(topic.percentage) : 0}% (${countLabel})`,
                fillStyle: Array.isArray(bgColors) ? bgColors[i] : '#ccc',
                strokeStyle: '#ccc',
                lineWidth: 1,
                hidden: false,
                index: i
              };
            }) ?? [];
          }
        }
      },
      tooltip: {
        enabled: true,
        callbacks: {
          label: (context) => {
            const label = context.label || '';
            const value = context.raw as number || 0;
            const topics = this.result()?.topics || [];
            const topic = topics.find((t: { topic: string }) => t.topic === label);
            const countLabel = topic ? `${topic.correctQuestions}/${topic.totalQuestions}` : '0/0';
            return `${label}: ${Math.ceil(value)}% (${countLabel})`;
          }
        }
      },
    },
  };

  barChartData: ChartData<'bar'> = {
    labels: ['Best Performance', 'Midlevel Performance', 'Miserable Performance'],
    datasets: [{
      label: 'No. of Subtopics',
      data: [0, 0, 0],
      backgroundColor: ['#4CAF50', '#FFC107', '#F44336']
    }]
  };

  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    scales: {
      x: {
        ticks: {
          font: { weight: 'bold' },
          color: '#000'
        },
        title: {
          display: true,
          text: 'Performance Category',
          font: { weight: 'bold' }
        }
      },
      y: {
        ticks: {
          font: { weight: 'bold' },
          color: '#000'
        },
        title: {
          display: true,
          text: 'No. of Subtopics',
          font: { weight: 'bold' }
        }
      }
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const category = context.label;
            const subtopics = this.performanceCategoryMap[category] || [];
            if (subtopics.length === 0) return 'No subtopics';
            const lines = [];
            for (let i = 0; i < subtopics.length; i += 2) {
              lines.push(subtopics.slice(i, i + 2).join(', '));
            }
            return [`Subtopics:`].concat(lines);
          }
        }
      }
    }
  };

  horizontalBarChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{
      label: 'Percentage',
      data: [],
      backgroundColor: '#36A2EB',
      barThickness: 15,
    }]
  };

  horizontalBarChartOptions: ChartOptions<'bar'> = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        min: 0,
        max: 100,
        title: {
          display: true,
          text: 'Percentage',
          font: { weight: 'bold' }
        },
        ticks: {
          color: '#000',
          font: { weight: 'bold' },
          // callback: (value: number) => `${value}%` 
        }
      },
      y: {
        ticks: {
          color: '#000',
          font: { weight: 'bold' }
        }
      }
    },
    plugins: {
      legend: {
        display: true,
        labels: {
          generateLabels: () => [
            { text: 'Best Performance', fillStyle: '#4CAF50', strokeStyle: '#ccc', lineWidth: 1 },
            { text: 'Midlevel Performance', fillStyle: '#FFC107', strokeStyle: '#ccc', lineWidth: 1 },
            { text: 'Miserable Performance', fillStyle: '#F44336', strokeStyle: '#ccc', lineWidth: 1 }
          ]
        }
      },
      tooltip: {
        callbacks: {
          label: (context) => `${context.label}: ${context.raw}%`
        }
      },
      datalabels: {
        anchor: 'end',
        align: 'right',
        color: '#000',
        font: { weight: 'bold', size: 10 },
        formatter: (value) => `${value}%`
      }
    }
  };

  ngOnInit() {
    this.examService.getExamResultById(this.id()).subscribe({
      next: (data) => {
        this.result.set(data);
        this.updateChartData(data);

        // ✅ Trigger confetti and animation if passed
        if (data.passed) {
          this.showCelebrationModal.set(true);
          setTimeout(() => this.launchConfetti(), 200);
        }
      }
    });

    history.pushState(null, "", window.location.href);
    this.location.onPopState(() => {
      history.pushState(null, "", window.location.href);
    });
  }

  launchConfetti() {
    confetti({
      particleCount: 120,
      spread: 90,
      origin: { y: 0.6 }
    });
  }

  updateChartData(data: any): void {
    const topics = data.topics;

    this.chartData.labels = topics.map((t: any) => t.topic);
    this.chartData.datasets[0].data = topics.map((t: any) => Math.ceil(t.percentage));
    this.chartData.datasets[0].backgroundColor = this.generateColors(topics.length);

    let best = 0, mid = 0, miserable = 0;

    topics.forEach((t: any) => {
      if (t.percentage >= 80) best++;
      else if (t.percentage < 10) miserable++;
      else mid++;
    });

    this.barChartData.datasets[0].data = [best, mid, miserable];

    this.horizontalBarChartData.labels = topics.map((t: any) => t.topic);
    this.horizontalBarChartData.datasets[0].data = topics.map((t: any) => Math.ceil(t.percentage));
    this.horizontalBarChartData.datasets[0].backgroundColor = topics.map((t: any) => {
      if (t.percentage >= 80) return '#4CAF50';
      else if (t.percentage <= 10) return '#F44336';
      return '#FFC107';
    });

    const sortedTopics = [...topics].sort((a: any, b: any) => a.percentage - b.percentage);
    const bottomTwo = sortedTopics.slice(0, 2);
    const lowScoreTopics = topics.filter((t: any) => t.percentage === 0 || t.percentage < 20);
    const allImprovementTopics = [...bottomTwo, ...lowScoreTopics];
    const uniqueTopicsMap = new Map<string, { topic: string; percentage: number }>();
    allImprovementTopics.forEach(topic => {
      if (!uniqueTopicsMap.has(topic.topic)) {
        uniqueTopicsMap.set(topic.topic, topic);
      }
    });
    this.improvementTopics = Array.from(uniqueTopicsMap.values());

    this.performanceCategoryMap = {
      'Best Performance': [],
      'Midlevel Performance': [],
      'Miserable Performance': []
    };

    topics.forEach((t: any) => {
      if (t.percentage >= 80) {
        this.performanceCategoryMap['Best Performance'].push(t.topic);
      } else if (t.percentage < 10) {
        this.performanceCategoryMap['Miserable Performance'].push(t.topic);
      } else {
        this.performanceCategoryMap['Midlevel Performance'].push(t.topic);
      }
    });

    this.barChartData.datasets[0].data = [
      this.performanceCategoryMap['Best Performance'].length,
      this.performanceCategoryMap['Midlevel Performance'].length,
      this.performanceCategoryMap['Miserable Performance'].length
    ];
  }

  generateColors(count: number): string[] {
    const colors: string[] = [];
    for (let i = 0; i < count; i++) {
      const hue = Math.round((360 / count) * i);
      colors.push(`hsl(${hue}, 70%, 60%)`);
    }
    return colors;
  }
}
