import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'alingo';

  get logged(): boolean {
    return localStorage.getItem("token") != undefined;
  }


  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  }
}
