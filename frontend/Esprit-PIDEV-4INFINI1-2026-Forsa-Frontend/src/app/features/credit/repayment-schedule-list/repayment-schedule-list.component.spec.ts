import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepaymentScheduleListComponent } from './repayment-schedule-list.component';

describe('RepaymentScheduleListComponent', () => {
  let component: RepaymentScheduleListComponent;
  let fixture: ComponentFixture<RepaymentScheduleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RepaymentScheduleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepaymentScheduleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
