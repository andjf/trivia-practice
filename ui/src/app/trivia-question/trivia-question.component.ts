import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { TriviaQuestion } from '../model/trivia-question.interface';
import { firstValueFrom } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { MatDialog } from '@angular/material/dialog';
import { ExplanationComponent } from '../explanation/explanation.component';

const DEFAULT_DURATION = 300;

@Component({
  selector: 'app-trivia-question',
  imports: [CommonModule, FormsModule],
  templateUrl: './trivia-question.component.html',
  styleUrls: ['./trivia-question.component.css'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(DEFAULT_DURATION + 'ms ease-in')),
      transition('true => false', animate(DEFAULT_DURATION + 'ms ease-out'))
    ])
  ],
})
export class TriviaQuestionComponent implements OnInit {

  @Input() topic!: string;
  @Input() difficulty!: string;

  question: TriviaQuestion | null = null;
  loading: boolean = true;
  selectedInput: string | null = null;
  @ViewChild('triviaForm') triviaForm!: NgForm;
  hasGuessed: boolean = false;

  constructor(private http: HttpClient, public dialog: MatDialog) { }

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
    if (this.triviaForm.valid && this.question && this.selectedInput !== null) {
      const selected = parseInt(this.selectedInput);

      this.hasGuessed = true;
      this.resetStyles();

      if (selected === this.question.answerIndex) {
        this.markCorrect(selected);
      } else {
        this.markIncorrect(selected);
        this.markCorrect(this.question.answerIndex);
      }
    }
  }

  resetStyles() {
    const options = document.querySelectorAll('.option');
    options.forEach((option) => {
      option.classList.remove('correct', 'incorrect');
    });
  }

  markCorrect(index: number) {
    const option = document.querySelector(`.option:nth-child(${index + 1})`);
    if (option) {
      option.classList.add('correct');
    }
  }

  markIncorrect(index: number) {
    const option = document.querySelector(`.option:nth-child(${index + 1})`);
    if (option) {
      option.classList.add('incorrect');
    }
  }

  newQuestion() {
    this.resetStyles();
    this.hasGuessed = false;
    this.triviaForm.resetForm();
    this.fetchQuestion();
  }

  openDialog() {
    console.log("opening dialog");
    const dialogRef = this.dialog.open(ExplanationComponent, {
      data: this.question,
      panelClass: 'explain-dialog',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
}