import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MainPageComponent } from './_core/containers/main-page/main-page.component';
import { HomePageComponent } from './_core/containers/home-page/home-page.component';
import { AuthGuard } from './auth/services/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: MainPageComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', component: HomePageComponent },
      {
        path: 'cuentas',
        loadChildren: './cuentas/cuentas.module#CuentasModule'
      },
      {
        path: 'egresos',
        loadChildren: './egresos/egresos.module#EgresosModule'
      },
      {
        path: 'ingresos',
        loadChildren: './ingresos/ingresos.module#IngresosModule'
      },
      {
        path: 'movimientos',
        loadChildren: './movimientos/movimientos.module#MovimientosModule'
      },
      {
        path: 'cortesTarjeta',
        loadChildren:
          './cortes-tarjeta/cortes-tarjeta.module#CortesTarjetaModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
