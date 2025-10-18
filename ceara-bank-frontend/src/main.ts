import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app.component';

// Este arquivo é crucial. Ele diz ao Angular para iniciar a aplicação
// usando o componente 'App' (que está em app.component.ts) como raiz.

bootstrapApplication(App)
  .catch((err) => console.error(err));
