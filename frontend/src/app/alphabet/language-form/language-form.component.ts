import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';
import { LanguageForm } from '../form/language-form';

@Component({
  selector: 'app-language-form',
  templateUrl: './language-form.component.html',
  styleUrls: ['./language-form.component.css']
})
export class LanguageFormComponent implements OnInit {
  form: FormGroup<LanguageForm>;
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private fb: FormBuilder, private modService: AlphabetModerationService) {
    this.form = this.fb.group({
      name: [new String(""), [Validators.required, Validators.minLength(1)]]
    });
   }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if(this.form.valid) {
      this.modService.createLanguage({ 
        name: this.form.controls.name.value ? this.form.controls.name.value : ""
      }).subscribe({
        next: (response: LanguageDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  onCreation(response: LanguageDetails): void {
    // TODO
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }


}
