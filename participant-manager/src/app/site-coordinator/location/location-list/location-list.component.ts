import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {LocationService} from '../shared/location.service';
import {Location, ManageLocations} from '../shared/location.model';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {of} from 'rxjs';
import {SearchService} from 'src/app/shared/search.service';
import {Permission} from 'src/app/shared/permission-enums';
import {DataTable} from 'angular-9-datatable';

@Component({
  selector: 'location-list',
  templateUrl: './location-list.component.html',
  styleUrls: ['./location-list.component.scss'],
})
export class LocationListComponent implements OnInit, AfterViewInit {
  query$ = new BehaviorSubject('');
  location$: Observable<ManageLocations> = of();
  manageLocationBackup = {} as ManageLocations;
  permission = Permission;
  constructor(
    private readonly locationService: LocationService,
    private readonly router: Router,
    private readonly toastr: ToastrService,
    private readonly sharedService: SearchService,
  ) {}
  @ViewChild('dataTable') dataTable: DataTable | undefined;


  ngAfterViewInit():void {
    console.log(this.dataTable)
       this.dataTable?.onPageChange.subscribe((x:unknown) =>{
        console.log(x);
        });
   }
   ngOnInit(): void {
    this.sharedService.updateSearchPlaceHolder('Search Location');
    this.getLocation();
  }

  getLocation(): void {
    this.location$ = combineLatest(
      this.locationService.getLocations(),
      this.query$,
    ).pipe(
      map(([manageLocations, query]) => {
        this.manageLocationBackup = {...manageLocations};
        this.manageLocationBackup.locations = this.manageLocationBackup.locations.filter(
          (location: Location) =>
            (location.name &&
              location.name.toLowerCase().includes(query.toLowerCase())) ||
            (location.customId &&
              location.customId.toLowerCase().includes(query.toLowerCase())),
        );
        return this.manageLocationBackup;
      }),
    );
  }
  search(query: string): void {
    this.query$.next(query.trim());
  }

  locationDetails(locationId: number): void {
    void this.router.navigate(['/coordinator/locations/', locationId]);
  }
  addLocation(): void {
    void this.router.navigate(['/coordinator/locations/new']);
  }
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  pageChangedAction(x:unknown) {
        console.log(x);
   }
}
