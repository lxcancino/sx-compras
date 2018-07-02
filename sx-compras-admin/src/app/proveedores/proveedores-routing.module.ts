import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import * as fromContainers from './containers';
import { ProveedoresGuard, ProveedorExistsGuard } from './guards';

const routes: Routes = [
  {
    path: '',
    component: fromContainers.ProveedoresPageComponent,
    canActivate: [ProveedoresGuard],
    children: [{ path: '', component: fromContainers.ProveedoresComponent }]
  },
  {
    path: ':proveedorId',
    canActivate: [ProveedoresGuard, ProveedorExistsGuard],
    component: fromContainers.ProveedorPageComponent,
    children: [
      { path: '', redirectTo: 'info', pathMatch: 'full' },
      {
        path: 'info',
        component: fromContainers.ProveedorInfoComponent
      },
      {
        path: 'productos',
        component: fromContainers.ProveedoProductosComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProveedoresRoutingModule {}
