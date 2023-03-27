import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { AuthResponse } from '../dto/auth-response';
import { LoginForm } from '../form/login-form';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  form: FormGroup<LoginForm>;
  isError: boolean = false;
  errorMsg: String = "";

  constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) { 
    this.form = this.fb.nonNullable.group({
      username: [new String(''), [Validators.required, Validators.minLength(1)]],
      password: [new String(''), [Validators.required, Validators.minLength(1)]]
    });
  }

  onAuth(response: AuthResponse): void {
    localStorage.setItem("token", response.token);
    localStorage.setItem("username", response.username);
    localStorage.setItem("moderator", response.moderation_role);
    this.router.navigate(["/"]);
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  login(): void {
    if(this.form.valid) {
      this.authService.authenticate({ 
        username: this.form.controls.username.value, 
        password: this.form.controls.password.value
      }).subscribe({
        next: (response: AuthResponse) => this.onAuth(response),
        error: (error: HttpErrorResponse) => this.onError(error)
      });
    }
  }

}
