import { animate, style, transition, trigger } from '@angular/animations';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { faCheckCircle, faCheckDouble, faCross, faHourglassEnd, faVolumeUp } from '@fortawesome/free-solid-svg-icons';
import { interval, Subscription } from 'rxjs';
import { AlphabetService } from '../alphabet.service';
import { AnswerResponse } from '../dto/answer-response';
import { Exercise } from '../dto/exercise';
import { ExercisesResponse } from '../dto/exercises-response';
import { SoundService } from '../sound.service';
import { Colors } from '../utils/colors';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css'],
  animations:  [trigger('iconAnim', [
    transition(':enter', [
      style({ opacity: 1, position: 'absolute', top: '150px', left: '50%' }),
      animate('500ms ease-in', style({ opacity: 0, transform: 'translateY(-100px)' }))
    ])
  ])]
})
export class GameComponent implements OnInit {
  exercises: Exercise[] = [];
  page: number = 1;
  courseId?: number;
  isError: boolean = false;
  errorMsg: String = "";
  current: number = 0;
  timer?: Subscription;
  timeFlag: boolean = false;
  correctFlag: boolean = false;
  showResult: boolean = false;
  colors: Colors = {left: {correct: false, wrong: false}, right: {correct: false, wrong: false}};
  soundIcon = faVolumeUp;
  timeIcon = faHourglassEnd;
  correctIcon = faCheckCircle;
  wrongIcon = faCross;
  sound?: HTMLAudioElement;
  started: boolean = false;

  constructor(private alphabetService: AlphabetService, private route: ActivatedRoute, private soundService: SoundService) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.courseId = routeParams['id'];
      this.getExercises(1, this.updateExercises.bind(this));
    });   
  }

  getExercises(page: number, onResponse: (response: ExercisesResponse) => void): void {
    if(this.courseId) {
      this.alphabetService.getExercises(this.courseId, page, 10).subscribe({
        next: (response: ExercisesResponse) => onResponse(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      })
    }
  }

  startGame(): void {
    this.started = true;
    this.getSound();
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  updateExercises(response: ExercisesResponse): void {
    this.isError = false;
    this.exercises = response.exercises;
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
    this.timer?.unsubscribe();
    this.correctFlag = response.correct;
    this.showResult = true;
    if(response.correct) {
      this.showCorrectColor(response);
    } else {
      this.showWrongColor(response);
    }
    this.timer  = interval(500).subscribe((_) => this.prepareNewExercises())
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

  private prepareNewExercises(): void {
    this.timer?.unsubscribe();
    let next = this.current + 1;
    if (next >= this.exercises.length) {
      this.page = this.page + 1;
      this.getExercises(this.page, this.newExercises.bind(this));
    } else {
      this.nextExercise();
    }
  }

  private newExercises(response: ExercisesResponse): void {
    this.updateExercises(response);
    this.cleanColors();
    this.current = 0;
    this.getSound();
  }

  private nextExercise() {
    this.cleanColors();
    this.current++;
    this.getSound();
  }

  animationDone(): void {
    this.timeFlag = false;
    this.showResult = false;
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
    this.timer?.unsubscribe();
    this.timeFlag = true;
    console.log("Time up!");
  }

  playSound(): void {
    this.sound?.play();
  }

  getSound(): void {
    this.soundService.getSound(this.exercises[this.current].soundFilename).subscribe({
      next: (response: Blob) => this.prepareSound(response)
    })
  }

  prepareSound(response: Blob): void {
    this.sound = this.soundService.prepareSound(response);
    this.playSound();
    this.timer  = interval(5000).subscribe((_) => this.timeUp());
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

  @HostListener('document:keydown.space', ['$event'])
  onSpaceDown(event: KeyboardEvent) {
    event.preventDefault();
    this.playSound();
  }
}
