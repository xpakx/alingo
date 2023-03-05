import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';

@Component({
  selector: 'app-languages',
  templateUrl: './languages.component.html',
  styleUrls: ['./languages.component.css']
})
export class LanguagesComponent implements OnInit {
  languages: LanguageDetails[] = [];
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private modService: AlphabetModerationService) { }

  ngOnInit(): void {
      this.loadLanguages();
  }

  loadLanguages() {
    this.modService.getLanguages().subscribe({
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
}
