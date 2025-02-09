import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { TriviaQuestion } from '../model/trivia-question.interface';
import { firstValueFrom } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms'; // Import FormsModule and NgForm

@Component({
  selector: 'app-trivia-question',
  imports: [CommonModule, FormsModule],
  templateUrl: './trivia-question.component.html',
  styleUrls: ['./trivia-question.component.css']
})
export class TriviaQuestionComponent implements OnInit {

  @Input() topic!: string;
  @Input() difficulty!: string;

  question: TriviaQuestion | null = null;
  loading: boolean = true;
  selectedAnswer: number | null = null; // Store the selected answer
  @ViewChild('triviaForm') triviaForm!: NgForm; // Get a reference to the form

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchQuestion();
  }

  async fetchQuestion() {
    this.loading = true;
    try {
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
      this.question = await firstValueFrom(obs);
    } catch (error) {
      console.error("Error fetching question:", error);
      this.question = null;
    } finally {
      this.loading = false;
    }
  }

  submitAnswer() {
    if (this.triviaForm.valid && this.question) {
      console.log("Selected Answer:", this.selectedAnswer, typeof this.selectedAnswer);
      console.log("Correct Answer:", this.question.answerIndex, typeof this.question.answerIndex);
      if (this.selectedAnswer == this.question.answerIndex) {
        alert("Correct!");
      } else {
        alert("Incorrect.");
      }
      this.triviaForm.resetForm();
      this.fetchQuestion();
    }
  }
}