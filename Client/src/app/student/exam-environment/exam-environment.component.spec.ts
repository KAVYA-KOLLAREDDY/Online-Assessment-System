import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamEnvironmentComponent } from './exam-environment.component';

describe('ExamEnvironmentComponent', () => {
  let component: ExamEnvironmentComponent;
  let fixture: ComponentFixture<ExamEnvironmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExamEnvironmentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExamEnvironmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
