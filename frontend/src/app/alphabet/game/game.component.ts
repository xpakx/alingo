import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AlphabetService } from '../alphabet.service';
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

  constructor(private alphabetService: AlphabetService) { }

  ngOnInit(): void {

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
}
