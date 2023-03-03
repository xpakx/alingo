import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ErrorInterceptor } from './auth/error.interceptor';
import { LoginComponent } from './auth/login/login.component';
import { RegistrationComponent } from './auth/registration/registration.component';
import { GameComponent } from './alphabet/game/game.component';
import { LanguageFormComponent } from './alphabet/language-form/language-form.component';
import { CourseFormComponent } from './alphabet/course-form/course-form.component';
import { ExerciseFormComponent } from './alphabet/exercise-form/exercise-form.component';
import { AddLanguageComponent } from './alphabet/add-language/add-language.component';
import { AddCourseComponent } from './alphabet/add-course/add-course.component';
import { AddExerciseComponent } from './alphabet/add-exercise/add-exercise.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegistrationComponent,
    GameComponent,
    LanguageFormComponent,
    CourseFormComponent,
    ExerciseFormComponent,
    AddLanguageComponent,
    AddCourseComponent,
    AddExerciseComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
