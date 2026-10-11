import { Routes } from '@angular/router';

import { AccountComponent } from './features/account/account.component';
import { AccountsComponent } from './features/accounts/accounts.component';
import { LandingComponent } from './features/landing/landing.component';
import { adminGuard, clientGuard } from './auth/role.guard';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'account', component: AccountComponent, canActivate: [clientGuard] },
  { path: 'accounts', component: AccountsComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' },
];
