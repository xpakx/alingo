import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllLanguagesComponent } from './all-languages.component';

describe('AllLanguagesComponent', () => {
  let component: AllLanguagesComponent;
  let fixture: ComponentFixture<AllLanguagesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllLanguagesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllLanguagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
