import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth-module').then(m => m.AuthModule),
  },
  {
    path: 'client',
    loadChildren: () => import('./features/client/client-module').then(m => m.ClientModule),
  },
  {
    path: 'agent',
    loadChildren: () => import('./features/agent/agent-module').then(m => m.AgentModule),
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin-module').then(m => m.AdminModule),
  },
  {
    path: '',
    redirectTo: '/auth',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/auth'
  }
];
