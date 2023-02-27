import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AlphabetService } from '../alphabet.service';
import { AnswerResponse } from '../dto/answer-response';
import { Exercise } from '../dto/exercise';
import { ExercisesResponse } from '../dto/exercises-response';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit {
  exercises: Exercise[] = [];
  page: number = 0;
  courseId?: number;
  isError: boolean = false;
  errorMsg: String = "";
  current: number = 0;

  constructor(private alphabetService: AlphabetService) { }

  ngOnInit(): void {
    this.getExercises(0);
  }

  getExercises(page: number): void {
    if(this.courseId) {
      this.alphabetService.getExercises(this.courseId, page, 10).subscribe({
        next: (response: ExercisesResponse) => this.updateExercises(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      })
    }
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  updateExercises(response: ExercisesResponse): void {
    this.isError = false;
    this.exercises = response.exercises;
    this.page = response.page;
  }

  guess(answer: String): void {
    if(this.courseId) {
      this.alphabetService.guess(this.exercises[this.current].id, { answer: answer}).subscribe({
        next: (response: AnswerResponse) => this.onAnswer(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      })
    }
  }

  onAnswer(response: AnswerResponse): void {
    if(response.correct) {

    } else {

    }
    this.nextExercise();
  }

  private nextExercise() {
    this.current++;
    if (this.current >= this.exercises.length) {
      this.current = 0;
      this.getExercises(this.page + 1);
    }
  }
}
