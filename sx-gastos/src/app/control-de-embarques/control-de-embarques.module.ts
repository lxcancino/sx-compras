import { NgModule } from '@angular/core';

import { SharedModule } from '../_shared/shared.module';
import { ReportesModule } from '../reportes/reportes.module';
import { ControlDeEmbarquesRoutingModule } from './control-de-embarques-routing.module';

import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { reducers, effects } from './store';

import { components, entryComponents } from './components';
import { containers } from './containers';
import { services } from './services';
import { guards } from './guards';

@NgModule({
  imports: [
    SharedModule,
    ControlDeEmbarquesRoutingModule,
    ReportesModule,
    StoreModule.forFeature('embarques', reducers),
    EffectsModule.forFeature(effects)
  ],
  declarations: [...components, ...containers],
  entryComponents: [...entryComponents],
  providers: [...services, ...guards]
})
export class ControlDeEmbarquesModule {}
