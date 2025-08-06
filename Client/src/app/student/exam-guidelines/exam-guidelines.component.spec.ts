import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamGuidelinesComponent } from './exam-guidelines.component';

describe('ExamGuidelinesComponent', () => {
  let component: ExamGuidelinesComponent;
  let fixture: ComponentFixture<ExamGuidelinesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExamGuidelinesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExamGuidelinesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
