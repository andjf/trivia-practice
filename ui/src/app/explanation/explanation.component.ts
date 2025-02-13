import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { filter, map, Subject } from 'rxjs';
import { TriviaQuestion } from '../model/trivia-question.interface';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { environment } from '../../environments/environment';
import { MarkdownModule } from 'ngx-markdown';
import { SseClient } from '../sse/sse.service';

@Component({
  selector: 'app-explanation',
  imports: [
    MatIconModule,
    CommonModule,
    MatProgressSpinnerModule,
    MarkdownModule,
  ],
  templateUrl: './explanation.component.html',
  styleUrl: './explanation.component.css'
})
export class ExplanationComponent {
  loading = true;
  tokens: string[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<ExplanationComponent>,
    @Inject(MAT_DIALOG_DATA) public question: TriviaQuestion,
    private sseClient: SseClient,
  ) { }

  getPromptBody(): string {
    return `
    I've been given the following question:

    > ${this.question.question}
    > 
    > with the following options:
    > 1. ${this.question.option1}
    > 2. ${this.question.option2}
    > 3. ${this.question.option3}
    > 4. ${this.question.option4}

    Please explan the question to me including some brief background
    context. Understanding that the option marked as ${this.question.answerIndex + 1}
    is the correct answer, explain why it is the correct answer.
    For all the other (incorrect) options, explain why they are incorrect
    (if applicable - meaning the option could be confused as a correct answer).

    Please use markdown format in your response. Keep your response to a few sentances.
    `;
  }

  get content() {
    return this.tokens.join('');
  }

  ngOnInit(): void {
    const url = `${environment.apiBaseUrl}/stream`

    const extractToken = (tokenJson: string): { token: string } => {
      console.log(`working on tokenJson <${tokenJson}>`)
      return JSON.parse(tokenJson);
    }

    const validData = (tokenJson: string): boolean => {
      try {
        JSON.parse(tokenJson);
        return !!tokenJson;
      } catch (err) {
        console.error(`failed to parse JSON from data <${tokenJson}>`);
        return false;
      }
    }

    this.sseClient.stream(url, this.getPromptBody())
      .pipe(filter(validData), map(extractToken))
      .subscribe((event) => {
        this.loading = false;
        this.tokens.push(event.token);
      });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
