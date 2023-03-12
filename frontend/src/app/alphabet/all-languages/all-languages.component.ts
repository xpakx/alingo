import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';
import { SearchForm } from '../form/search-form';

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
  form: FormGroup<SearchForm>;
  searchMode: boolean = false;

  constructor(private modService: AlphabetModerationService, private fb: FormBuilder) {
    this.form = this.fb.nonNullable.group({
      name: [new String("")]
    });
   }

  ngOnInit(): void {
      this.loadLanguages(this.page);
  }

  loadLanguages(page: number) {
    this.modService.getLanguages(page).subscribe({
      next: (response: LanguageDetails[]) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  loadLanguagesForName(page: number) {
    this.modService.findLanguages(page, this.form.controls.name.value).subscribe({
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
    if(this.searchMode) {
      this.loadLanguagesForName(++this.page);
    } else {
      this.loadLanguages(++this.page);
    }
  }

  prevPage(): void {
    if(this.searchMode) {
      this.loadLanguagesForName(--this.page);
    } else {
      this.loadLanguages(--this.page);
    }
  }

  chooseLanguage(id: number): void {
    this.id.emit(id);
  }

  deactivateSearch() {
    this.page = 1;
    this.searchMode = false;
    this.loadLanguages(this.page);
  }

  activateSearch() {
    this.page = 1;
    this.searchMode = true;
    this.loadLanguagesForName(this.page);
  }
}
