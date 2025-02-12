import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { MAT_DIALOG_DEFAULT_OPTIONS, MatDialogConfig } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { routes } from './app.routes';
import { MarkdownModule, MERMAID_OPTIONS } from 'ngx-markdown';

const defaultDialogConfig: MatDialogConfig = {
  disableClose: false,
  hasBackdrop: true,
  position: {
    top: '100px',
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptorsFromDi()),
    importProvidersFrom([
      BrowserAnimationsModule,
      MatIconModule,
      MatProgressSpinnerModule,
      MarkdownModule.forRoot({
        mermaidOptions: {
          provide: MERMAID_OPTIONS,
          useValue: {
            darkMode: true,
            look: 'handDrawn',
          },
        },
      })
    ]),
    provideAnimationsAsync(),
    {
      provide: MAT_DIALOG_DEFAULT_OPTIONS,
      useValue: defaultDialogConfig,
    },
  ]
};
