import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SoundService {
  private apiServerUrl = environment.apiUri;

  constructor() { }

  playSound(name: String): void {
    let audio = new Audio();
    audio.src = this.apiServerUrl + "/sound/" + name;
    audio.load();
    audio.play();
  }
}
