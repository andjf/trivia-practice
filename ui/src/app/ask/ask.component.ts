import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './ask.component.html',
  styleUrl: './ask.component.css'
})
export class AskComponent {
  topic: string = '';
  difficulty: string = '';

  constructor(private router: Router) { }

  canSubmit() {
    return this.topic.length && this.difficulty.length;
  }

  async onSubmit() {
    if (!this.canSubmit()) {
      return
    }

    const queryParams = {
      topic: this.topic,
      difficulty: this.difficulty,
    };
    this.router.navigate(['/question'], { queryParams });
  }
}
