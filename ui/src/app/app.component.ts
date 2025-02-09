import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  topic: string = '';
  difficulty: string = '';

  http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  async onSubmit() {
    const url = `${environment.apiBaseUrl}/v1/chat`;
    const obs = this.http.post(
      url,
      {
        topic: this.topic,
        difficulty: this.difficulty,
      },
    );
    const res = await firstValueFrom(obs);
    console.log(res);
  }
}
