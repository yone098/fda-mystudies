import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SiteCoordinatorRoutingModule} from './site-coordinator-routing.module';
import {SiteCoordinatorComponent} from './sitecoordinator.component';
import {FormsModule} from '@angular/forms';
import {NG2DataTableModule} from 'angular2-datatable-pagination';
import {MobileMenuComponent} from './mobile-menu/mobile-menu.component';
import {DashboardHeaderComponent} from './dashboard-header/dashboard-header.component';
import {ParticipantDetailsComponent} from './participant-details/participant-details.component';

@NgModule({
  declarations: [
    SiteCoordinatorComponent,
    MobileMenuComponent,
    DashboardHeaderComponent,
    ParticipantDetailsComponent,
  ],
  imports: [
    CommonModule,
    SiteCoordinatorRoutingModule,
    FormsModule,
    NG2DataTableModule,
  ],
})
export class SiteCoordinatorModule {}
