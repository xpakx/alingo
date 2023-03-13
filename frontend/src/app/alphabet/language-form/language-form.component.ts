import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { LanguageDetails } from '../dto/language-details';
import { LanguageForm } from '../form/language-form';

@Component({
  selector: 'app-language-form',
  templateUrl: './language-form.component.html',
  styleUrls: ['./language-form.component.css']
})
export class LanguageFormComponent implements OnInit{
  form: FormGroup<LanguageForm>;
  isError: boolean = false;
  errorMsg: String = "";
  @Input("language") language?: LanguageDetails;

  constructor(private fb: FormBuilder, private modService: AlphabetModerationService, private router: Router) {
    this.form = this.fb.group({
      name: [new String(""), [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    if(this.language) {
      this.form.setValue({name: this.language.name});
    }
  }

  onSubmit(): void {
    if(this.language) {
      this.updateLanguage(this.language.id);
    } else {
      this.createLanguage();
    }
  }

  updateLanguage(id: number): void {
    if(this.form.valid) {
      this.modService.updateLanguage(id, { 
        name: this.form.controls.name.value ? this.form.controls.name.value : ""
      }).subscribe({
        next: (response: LanguageDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  createLanguage(): void {
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
    this.router.navigate(['/language/'+response.id]);
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }
}
