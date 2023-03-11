import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { AlphabetService } from '../alphabet.service';
import { AnswerResponse } from '../dto/answer-response';
import { Exercise } from '../dto/exercise';
import { ExercisesResponse } from '../dto/exercises-response';
import { Colors } from '../utils/colors';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit {
  exercises: Exercise[] = []; //{id: 0, options: ["לָ", "מָ"]}]
  page: number = 0;
  courseId?: number;
  isError: boolean = false;
  errorMsg: String = "";
  current: number = 0;
  timer?: Subscription;
  timeFlag: boolean = false;
  colors: Colors = {left: {correct: false, wrong: false}, right: {correct: false, wrong: false}};

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
    this.timeFlag = false;
    this.timer?.unsubscribe();
    if(response.correct) {
      this.showCorrectColor(response);
    } else {
      this.showWrongColor(response);
    }
    this.timer  = interval(500).subscribe((_) => this.nextExercise())
  }

  private showWrongColor(response: AnswerResponse) {
    if (response.correctAnswer == this.exercises[this.current].options[0]) {
      this.colors.right.wrong = true;
    } else {
      this.colors.left.wrong = true;
    }
  }

  private showCorrectColor(response: AnswerResponse) {
    if (response.correctAnswer == this.exercises[this.current].options[0]) {
      this.colors.left.correct = true;
    } else {
      this.colors.right.correct = true;
    }
  }

  private nextExercise() {
    this.cleanColors();
    this.current++;
    if (this.current >= this.exercises.length) {
      this.current = 0;
      this.getExercises(this.page + 1);
    }
    this.timer  = interval(5000).subscribe((_) => this.timeUp())
  }

  cleanColors() {
    this.colors.left.correct = false;
    this.colors.left.wrong = false;
    this.colors.right.correct = false;
    this.colors.right.wrong = false;
  }

  onGuess(number: number) {
    console.log(number)
    this.guess(this.exercises[this.current].options[number]);
  }

  timeUp(): void {
    this.timer?.unsubscribe;
    this.timeFlag = true;
  }

  @HostListener('document:keydown.arrowleft', ['$event'])
  @HostListener('document:keydown.a', ['$event'])
  onLeftArrowDown(event: KeyboardEvent) {
    event.preventDefault();
    this.onGuess(0);
  }

  @HostListener('document:keydown.arrowright', ['$event'])
  @HostListener('document:keydown.d', ['$event'])
  onRightArrowDown(event: KeyboardEvent) {
    event.preventDefault();
    this.onGuess(1);
  }
}
