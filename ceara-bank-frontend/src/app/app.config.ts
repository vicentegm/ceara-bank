import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Routes } from '@angular/router'; // Importamos 'Routes'

// Definimos explicitamente que é um array de Routes (o que resolve o erro TS7034/TS7005)
const routes: Routes = []; 

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes)
  ]
};
