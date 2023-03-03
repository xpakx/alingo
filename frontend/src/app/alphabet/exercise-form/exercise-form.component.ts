import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { ExerciseDetails } from '../dto/exercise-details';
import { ExerciseForm } from '../form/exercise-form';

@Component({
  selector: 'app-exercise-form',
  templateUrl: './exercise-form.component.html',
  styleUrls: ['./exercise-form.component.css']
})
export class ExerciseFormComponent implements OnInit {
  form: FormGroup<ExerciseForm>;
  isError: boolean = false;
  errorMsg: String = "";
  @Input("exercise") exercise?: ExerciseDetails;

  constructor(private fb: FormBuilder, private modService: AlphabetModerationService) {
    this.form = this.fb.nonNullable.group({
      letter: [new String(""), [Validators.required, Validators.minLength(1)]],
      wrongAnswer: [new String(""), [Validators.required, Validators.minLength(1)]],
      correctAnswer: [new String(""), [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    if(this.exercise) {
      this.form.setValue({
        letter: this.exercise.letter, 
        wrongAnswer: this.exercise.wrongAnswer, 
        correctAnswer: this.exercise.correctAnswer
      });
    }
  }

  onSubmit(): void {
    if(this.exercise) {
      this.updateExercise(this.exercise.id);
    } else {
      this.createExercise();
    }
  }

  updateExercise(id: number): void {
    if(this.form.valid) {
      this.modService.updateExercise(id, { 
        letter: this.form.controls.letter.value,
        wrongAnswer: this.form.controls.wrongAnswer.value,
        correctAnswer: this.form.controls.correctAnswer.value,
        courseId: 0
      }).subscribe({
        next: (response: ExerciseDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  createExercise(): void {
    if(this.form.valid) {
      this.modService.createExercise({ 
        letter: this.form.controls.letter.value,
        wrongAnswer: this.form.controls.wrongAnswer.value,
        correctAnswer: this.form.controls.correctAnswer.value,
        courseId: 0
      }).subscribe({
        next: (response: ExerciseDetails) => this.onCreation(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

  onCreation(response: ExerciseDetails): void {
    // TODO
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }
}
