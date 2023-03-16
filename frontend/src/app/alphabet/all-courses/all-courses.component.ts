import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { CourseDetails } from '../dto/course-details';
import { CourseList } from '../dto/course-list';
import { SearchForm } from '../form/search-form';

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
  form: FormGroup<SearchForm>;
  searchMode: boolean = false;

  constructor(private modService: AlphabetModerationService, private fb: FormBuilder) {
    this.form = this.fb.nonNullable.group({
      name: [new String("")]
    });
   }

  ngOnInit(): void {
      this.loadCourses(this.page);
  }

  loadCourses(page: number) {
    this.modService.getCourses(page).subscribe({
      next: (response: CourseList) => this.onResponse(response.courses),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  loadCoursesForName(page: number) {
    this.modService.findCourses(page, this.form.controls.name.value).subscribe({
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
    if(this.searchMode) {
      this.loadCoursesForName(++this.page);
    } else {
      this.loadCourses(++this.page);
    }
  }

  prevPage(): void {
    if(this.searchMode) {
      this.loadCoursesForName(--this.page);
    } else {
      this.loadCourses(--this.page);
    }
  }

  chooseCourse(id: number): void {
    this.id.emit(id);
  }

  deactivateSearch() {
    this.page = 1;
    this.searchMode = false;
    this.loadCourses(this.page);
  }

  activateSearch() {
    this.page = 1;
    this.searchMode = true;
    this.loadCoursesForName(this.page);
  }
}
