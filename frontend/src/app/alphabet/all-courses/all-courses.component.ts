import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { CourseDetails } from '../dto/course-details';

@Component({
  selector: 'app-all-courses',
  templateUrl: './all-courses.component.html',
  styleUrls: ['./all-courses.component.css']
})
export class AllCoursesComponent implements OnInit {
  @Output("courseId") id: EventEmitter<number> = new EventEmitter<number>();
  courses: CourseDetails[] = [];
  isError: boolean = false;
  errorMsg: String = "";
  page: number = 1;

  constructor(private modService: AlphabetModerationService) { }

  ngOnInit(): void {
      this.loadCourses(this.page);
  }

  loadCourses(page: number) {
    this.modService.getCourses(page).subscribe({
      next: (response: CourseDetails[]) => this.onResponse(response),
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

  chooseCourse(id: number): void {
    this.id.emit(id);
  }
}
