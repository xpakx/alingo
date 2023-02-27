import { TestBed } from '@angular/core/testing';

import { AlphabetModerationService } from './alphabet-moderation.service';

describe('AlphabetModerationService', () => {
  let service: AlphabetModerationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AlphabetModerationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
