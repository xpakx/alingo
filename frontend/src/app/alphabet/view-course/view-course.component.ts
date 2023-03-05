import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlphabetModerationService } from '../alphabet-moderation.service';
import { CourseData } from '../dto/course-data';

@Component({
  selector: 'app-view-course',
  templateUrl: './view-course.component.html',
  styleUrls: ['./view-course.component.css']
})
export class ViewCourseComponent implements OnInit {
  course?: CourseData;
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private route: ActivatedRoute, private modService: AlphabetModerationService) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadCourse(routeParams['id']);
    });   
  }

  loadCourse(id: number) {
    this.modService.getCourse(id).subscribe({
      next: (response: CourseData) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }

  onResponse(response: CourseData): void {
    this.course = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }
}
