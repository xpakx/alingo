import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { AuthResponse } from '../dto/auth-response';
import { RegistrationForm } from '../form/registration-form';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent {
  form: FormGroup<RegistrationForm>;
  isError: boolean = false;
  errorMsg: String = "";


  constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) {
    this.form = this.fb.nonNullable.group({
      username: [new String(''), Validators.required],
      password: [new String(''), Validators.required],
      passwordRe: [new String(''), Validators.required]
    });
  }

  onAuth(response: AuthResponse): void {
    localStorage.setItem("token", response.token);
    localStorage.setItem("username", response.username);
    this.router.navigate(["/"]);
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  register(): void {
    if(this.form.valid) {
      this.authService.register({ 
        username: this.form.controls.username.value, 
        password: this.form.controls.password.value,
        passwordRe: this.form.controls.passwordRe.value
      }).subscribe({
        next: (response: AuthResponse) => this.onAuth(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

}
