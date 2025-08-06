import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrackExamsComponent } from './track-exams.component';

describe('TrackExamsComponent', () => {
  let component: TrackExamsComponent;
  let fixture: ComponentFixture<TrackExamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackExamsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrackExamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
