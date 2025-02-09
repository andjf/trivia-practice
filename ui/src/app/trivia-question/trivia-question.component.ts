import { Component, Input } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { TriviaQuestion } from '../model/trivia-question.interface';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-trivia-question',
  imports: [],
  templateUrl: './trivia-question.component.html',
  styleUrl: './trivia-question.component.css'
})
export class TriviaQuestionComponent {

  @Input() topic!: string;
  @Input() difficulty!: string;

  http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  async do() {
    const url = `${environment.apiBaseUrl}/trivia/new`;
    const obs = this.http.get<TriviaQuestion>(
      url,
      {
        params: {
          "topic": this.topic,
          "difficulty": this.difficulty,
        }
      }
    );
    const res = await firstValueFrom(obs);
    console.log(res);
  }

}
