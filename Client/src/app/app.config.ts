import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { ToastNoAnimation, ToastNoAnimationModule } from 'ngx-toastr';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { provideToastr } from 'ngx-toastr';

import { NGX_MONACO_EDITOR_CONFIG, NgxMonacoEditorConfig } from 'ngx-monaco-editor';

// ✅ Monaco Editor Config
const monacoConfig: NgxMonacoEditorConfig = {
  baseUrl: 'assets', // ✅ Matches output path in angular.json
  defaultOptions: {
    scrollBeyondLastLine: false,
    automaticLayout: true,
  }
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideCharts(withDefaultRegisterables()),
    provideToastr({ toastComponent: ToastNoAnimation }),
    { provide: NGX_MONACO_EDITOR_CONFIG, useValue: monacoConfig } 
  ],
};
