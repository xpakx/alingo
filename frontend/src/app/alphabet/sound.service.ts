import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { JwtService } from '../common/jwt-service';

@Injectable({
  providedIn: 'root'
})
export class SoundService extends JwtService {
  private apiServerUrl = environment.apiUri;

  constructor(private http: HttpClient) { 
    super();
  }

  public getSound(name: String): Observable<Blob> {
    return this.http.get(`${this.apiServerUrl}/sound/${name}`,  { headers: this.getHeaders(), responseType: 'blob' });
  }

  playSound(blob: Blob): void {
    let audio = new Audio();
    audio.src = URL.createObjectURL(blob);
    audio.load();
    audio.play();
  }
}
