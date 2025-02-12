import { Routes } from '@angular/router';
import { TriviaQuestionComponent } from './trivia-question/trivia-question.component';
import { AskComponent } from './ask/ask.component';

export const routes: Routes = [
    {
        path: '',
        redirectTo: '/ask',
        pathMatch: 'full',
    },
    {
        path: 'ask',
        component: AskComponent,
    },
    {
        path: 'question',
        component: TriviaQuestionComponent,
    },
];
