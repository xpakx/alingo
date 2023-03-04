import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { JwtService } from '../common/jwt-service';
import { CourseData } from './dto/course-data';
import { CourseDetails } from './dto/course-details';
import { CourseRequest } from './dto/course-request';
import { ExerciseData } from './dto/exercise-data';
import { ExerciseDetails } from './dto/exercise-details';
import { ExerciseRequest } from './dto/exercise-request';
import { LanguageDetails } from './dto/language-details';
import { LanguageRequest } from './dto/language-request';
import { OrderRequest } from './dto/order-request';

@Injectable({
  providedIn: 'root'
})
export class AlphabetModerationService extends JwtService {
  private apiServerUrl = environment.apiUri;

  constructor(private http: HttpClient) { 
    super();
  }

  public createLanguage(request: LanguageRequest): Observable<LanguageDetails> {
    return this.http.post<LanguageDetails>(`${this.apiServerUrl}/language`, request, { headers: this.getHeaders() });
  }

  public updateLanguage(languageId: number, request: LanguageRequest): Observable<LanguageDetails> {
    return this.http.put<LanguageDetails>(`${this.apiServerUrl}/language/${languageId}`, request, { headers: this.getHeaders() });
  }

  public getLanguage(languageId: number): Observable<LanguageDetails> {
    return this.http.get<LanguageDetails>(`${this.apiServerUrl}/language/${languageId}`, { headers: this.getHeaders() });
  }

  public createCourse(request: CourseRequest): Observable<CourseDetails> {
    return this.http.post<CourseDetails>(`${this.apiServerUrl}/course`, request, { headers: this.getHeaders() });
  }

  public updateCourse(courseId: number, request: CourseRequest): Observable<CourseDetails> {
    return this.http.put<CourseDetails>(`${this.apiServerUrl}/course/${courseId}`, request, { headers: this.getHeaders() });
  }

  public getCourse(courseId: number): Observable<CourseData> {
    return this.http.get<CourseData>(`${this.apiServerUrl}/course/${courseId}`, { headers: this.getHeaders() });
  }

  public createExercise(request: ExerciseRequest): Observable<ExerciseDetails> {
    return this.http.post<ExerciseDetails>(`${this.apiServerUrl}/exercise/new`, request, { headers: this.getHeaders() });
  }

  public updateExercise(exerciseId: number, request: ExerciseRequest): Observable<ExerciseDetails> {
    return this.http.put<ExerciseDetails>(`${this.apiServerUrl}/exercise/${exerciseId}`, request, { headers: this.getHeaders() });
  }

  public getExercise(exerciseId: number): Observable<ExerciseData> {
    return this.http.get<ExerciseData>(`${this.apiServerUrl}/exercise/${exerciseId}`, { headers: this.getHeaders() });
  }

  public reorder(exerciseId: number, request: OrderRequest): Observable<ExerciseDetails> {
    return this.http.put<ExerciseDetails>(`${this.apiServerUrl}/exercise/${exerciseId}/order`, request, { headers: this.getHeaders() });
  }
}
