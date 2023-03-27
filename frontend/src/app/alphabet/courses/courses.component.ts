import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { CourseData } from '../dto/course-data';
import { CourseDetails } from '../dto/course-details';
import { CourseList } from '../dto/course-list';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class CoursesComponent implements OnInit {
  courses: CourseDetails[] = [];
  isError: boolean = false;
  errorMsg: String = "";
  page: number = 1;
  languageId?: number;

  constructor(private modService: AlphabetModerationService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.languageId = routeParams['id']
      this.loadCourses(this.page);
    });  
  }

  loadCourses(page: number) {
    if(!this.languageId) {
      return;
    }
    this.modService.getCoursesForLanguage(this.languageId, page).subscribe({
      next: (response: CourseList) => this.onResponse(response.courses),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  onResponse(response: CourseDetails[]): void {
    this.courses = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  nextPage(): void {
    this.loadCourses(++this.page);
  }

  prevPage(): void {
    this.loadCourses(--this.page);
  }

  get moderator(): boolean {
    return localStorage.getItem("moderator") == 'true';
  }
}
