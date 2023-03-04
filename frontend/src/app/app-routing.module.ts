import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddCourseComponent } from './alphabet/add-course/add-course.component';
import { AddExerciseComponent } from './alphabet/add-exercise/add-exercise.component';
import { AddLanguageComponent } from './alphabet/add-language/add-language.component';
import { EditLanguageComponent } from './alphabet/edit-language/edit-language.component';
import { GameComponent } from './alphabet/game/game.component';
import { LoginComponent } from './auth/login/login.component';
import { RegistrationComponent } from './auth/registration/registration.component';

const routes: Routes = [
  { path: '', component: GameComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegistrationComponent },
  { path: 'language/add', component: AddLanguageComponent },
  { path: 'language/:id/edit', component: EditLanguageComponent },
  { path: 'course/add', component: AddCourseComponent },
  { path: 'exercise/add', component: AddExerciseComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
