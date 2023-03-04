import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { ExerciseData } from '../dto/exercise-data';
import { ExerciseDetails } from '../dto/exercise-details';

@Component({
  selector: 'app-edit-exercise',
  templateUrl: './edit-exercise.component.html',
  styleUrls: ['./edit-exercise.component.css']
})
export class EditExerciseComponent implements OnInit {
  exercise?: ExerciseDetails;
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private route: ActivatedRoute, private modService: AlphabetModerationService) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadExercise(routeParams['id']);
    });   
  }

  loadExercise(id: number) {
    this.modService.getExercise(id).subscribe({
      next: (response: ExerciseData) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  onResponse(response: ExerciseData): void {
    this.exercise = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }
}
