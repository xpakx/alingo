import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';

@Component({
  selector: 'app-all-languages',
  templateUrl: './all-languages.component.html',
  styleUrls: ['./all-languages.component.css']
})
export class AllLanguagesComponent implements OnInit {
  @Output("languageId") id: EventEmitter<number> = new EventEmitter<number>();
  languages: LanguageDetails[] = [];
  isError: boolean = false;
  errorMsg: String = "";
  page: number = 1;

  constructor(private modService: AlphabetModerationService) { }

  ngOnInit(): void {
      this.loadLanguages(this.page);
  }

  loadLanguages(page: number) {
    this.modService.getLanguages(page).subscribe({
      next: (response: LanguageDetails[]) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  onResponse(response: LanguageDetails[]): void {
    this.languages = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  nextPage(): void {
    this.loadLanguages(++this.page);
  }

  prevPage(): void {
    this.loadLanguages(--this.page);
  }

  chooseLanguage(id: number): void {
    this.id.emit(id);
  }
}
