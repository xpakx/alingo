import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddCourseComponent } from './alphabet/add-course/add-course.component';
import { AddExerciseComponent } from './alphabet/add-exercise/add-exercise.component';
import { AddLanguageComponent } from './alphabet/add-language/add-language.component';
import { CoursesComponent } from './alphabet/courses/courses.component';
import { EditCourseComponent } from './alphabet/edit-course/edit-course.component';
import { EditExerciseComponent } from './alphabet/edit-exercise/edit-exercise.component';
import { EditLanguageComponent } from './alphabet/edit-language/edit-language.component';
import { GameComponent } from './alphabet/game/game.component';
import { LanguagesComponent } from './alphabet/languages/languages.component';
import { ViewCourseComponent } from './alphabet/view-course/view-course.component';
import { ViewExerciseComponent } from './alphabet/view-exercise/view-exercise.component';
import { ViewLanguageComponent } from './alphabet/view-language/view-language.component';
import { LoginComponent } from './auth/login/login.component';
import { RegistrationComponent } from './auth/registration/registration.component';

const routes: Routes = [
  { path: '', component: GameComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegistrationComponent },
  { path: 'language/add', component: AddLanguageComponent },
  { path: 'language/:id/edit', component: EditLanguageComponent },
  { path: 'language/:id', component: ViewLanguageComponent },
  { path: 'language/:id/courses', component: CoursesComponent },
  { path: 'language/all', component: LanguagesComponent },
  { path: 'course/add', component: AddCourseComponent },
  { path: 'course/:id/edit', component: EditCourseComponent },
  { path: 'course/:id', component: ViewCourseComponent },
  { path: 'exercise/add', component: AddExerciseComponent },
  { path: 'exercise/:id/edit', component: EditExerciseComponent },
  { path: 'exercise/:id', component: ViewExerciseComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
