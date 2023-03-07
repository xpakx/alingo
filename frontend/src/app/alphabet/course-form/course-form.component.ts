import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { CourseData } from '../dto/course-data';
import { CourseDetails } from '../dto/course-details';
import { CourseForm } from '../form/course-form';

@Component({
  selector: 'app-course-form',
  templateUrl: './course-form.component.html',
  styleUrls: ['./course-form.component.css']
})
export class CourseFormComponent implements OnInit {
  form: FormGroup<CourseForm>;
  isError: boolean = false;
  errorMsg: String = "";
  @Input("course") course?: CourseData;

  constructor(private fb: FormBuilder, private modService: AlphabetModerationService) {
    this.form = this.fb.nonNullable.group({
      name: [new String(""), [Validators.required, Validators.minLength(1)]],
      description: [new String("")],
      difficulty: [new String("EASY")],
      languageId: [new Number(), Validators.required]
    });
  }

  ngOnInit(): void {
    if(this.course) {
      this.form.setValue({
        name: this.course.name, 
        description: this.course.description, 
        difficulty: this.course.difficulty,
        languageId: this.course.language.id
      });
    }
  }

  onSubmit(): void {
    if(this.course) {
      this.updateCourse(this.course.id);
    } else {
      this.createCourse();
    }
  }

  updateCourse(id: number): void {
    if(this.form.valid) {
      this.modService.updateCourse(id, { 
        name: this.form.controls.name.value,
        description: this.form.controls.description.value,
        difficulty: this.form.controls.difficulty.value,
        languageId: this.form.controls.languageId.value
      }).subscribe({
        next: (response: CourseDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  createCourse(): void {
    if(this.form.valid) {
      this.modService.createCourse({ 
        name: this.form.controls.name.value,
        description: this.form.controls.description.value,
        difficulty: this.form.controls.difficulty.value,
        languageId: 0
      }).subscribe({
        next: (response: CourseDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  onCreation(response: CourseDetails): void {
    // TODO
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  onLanguageChoice(id: number): void {
    this.form.patchValue({
      languageId: id
    });
  }
}
