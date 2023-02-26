import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { JwtService } from '../common/jwt-service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiServerUrl = environment.apiUri;

  constructor(private http: HttpClient) { }

  public authenticate(request: AuthenticationRequest):  Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiServerUrl}/authenticate`, request);
  }

  public register(request: RegistrationRequest):  Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiServerUrl}/register`, request);
  }

}
