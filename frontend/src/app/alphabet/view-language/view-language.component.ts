import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';

@Component({
  selector: 'app-view-language',
  templateUrl: './view-language.component.html',
  styleUrls: ['./view-language.component.css']
})
export class ViewLanguageComponent implements OnInit {
  language?: LanguageDetails;
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private route: ActivatedRoute, private modService: AlphabetModerationService) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadLanguage(routeParams['id']);
    });   
  }

  loadLanguage(id: number) {
    this.modService.getLanguage(id).subscribe({
      next: (response: LanguageDetails) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  onResponse(response: LanguageDetails): void {
    this.language = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }
}
