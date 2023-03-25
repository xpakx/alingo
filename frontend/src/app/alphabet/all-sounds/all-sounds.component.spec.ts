import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllSoundsComponent } from './all-sounds.component';

describe('AllSoundsComponent', () => {
  let component: AllSoundsComponent;
  let fixture: ComponentFixture<AllSoundsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllSoundsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllSoundsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
