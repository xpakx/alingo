import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { JwtService } from '../common/jwt-service';
import { AnswerRequest } from './dto/answer-request';
import { AnswerResponse } from './dto/answer-response';
import { ExercisesResponse } from './dto/exercises-response';

@Injectable({
  providedIn: 'root'
})
export class AlphabetService extends JwtService {
  private apiServerUrl = environment.apiUri;

  constructor(private http: HttpClient) { 
    super();
  }

  public getExercises(courseId: number, page: number, amount: number): Observable<ExercisesResponse> {
    let params = new HttpParams().set('page', page).set('amount', amount);
    return this.http.get<ExercisesResponse>(`${this.apiServerUrl}/course/${courseId}/exercise`,  { headers: this.getHeaders(), params: params });
  }

  public guess(exerciseId: number, request: AnswerRequest): Observable<AnswerResponse> {
    return this.http.post<AnswerResponse>(`${this.apiServerUrl}/exercise/${exerciseId}`, request, { headers: this.getHeaders() });
  }
}
