/* eslint-disable @typescript-eslint/no-unsafe-assignment */
import {
  async,
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import {AddEmailComponent} from './add-email.component';
import {HttpClientModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ToastrModule} from 'ngx-toastr';
import {SiteDetailsService} from '../shared/site-details.service';
import {of} from 'rxjs';
import {EntityService} from 'src/app/service/entity.service';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import * as expectedResult from 'src/app/entity/mock-sitedetail-data';
import {SitesModule} from '../sites.module';

describe('AddEmailComponent', () => {
  let component: AddEmailComponent;
  let fixture: ComponentFixture<AddEmailComponent>;
  let addParticipant: DebugElement;
  let cancel: DebugElement;
  let emailInput: DebugElement;
  beforeEach(async(async () => {
    const siteDetailsServiceSpy = jasmine.createSpyObj<SiteDetailsService>(
      'SiteDetailsService',
      {addParticipants: of(expectedResult.expectedAddParticipantResponse)},
    );
    await TestBed.configureTestingModule({
      declarations: [AddEmailComponent],
      imports: [
        SitesModule,
        BrowserAnimationsModule,
        HttpClientModule,
        ToastrModule.forRoot({
          positionClass: 'toast-top-center',
          preventDuplicates: true,
          enableHtml: true,
        }),
      ],
      providers: [
        EntityService,
        {provide: SiteDetailsService, useValue: siteDetailsServiceSpy},
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddEmailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    addParticipant = fixture.debugElement.query(By.css('[name="addEmail"]'));
    // emailInput = fixture.debugElement.query(By.css('#email'));
    emailInput = fixture.debugElement.query(By.css('[name="email"]'));
    cancel = fixture.debugElement.query(By.css('[name="cancel"]'));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should bind input email value to Component property', () => {
    const emailInputs = emailInput.nativeElement as HTMLInputElement;
    fixture.detectChanges();
    emailInputs.value = 'prak@grr.la';
    emailInputs.dispatchEvent(new Event('input'));
  });

  it('should add the particpants when add participants clicked', fakeAsync(async () => {
    component.addParticipant();
    const addParticipantSpy = spyOn(component, 'addParticipant');
    const addParticipantButton = addParticipant.nativeElement as HTMLInputElement;
    const emailInputs = emailInput.nativeElement as HTMLInputElement;
    emailInputs.value = 'prak@grr.la';
    dispatchEvent(new Event('input'));
    fixture.detectChanges();
    tick(10000);
    addParticipantButton.click();
    tick(10000);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(addParticipantSpy).toHaveBeenCalledTimes(1);
  }));

  it('should hide component onclick cancel button', fakeAsync(async () => {
    component.cancel();
    const cancelSpy = spyOn(component, 'cancel');
    const cancelButton = cancel.nativeElement as HTMLInputElement;
    fixture.detectChanges();
    tick(10000);
    cancelButton.click();
    fixture.detectChanges();
    tick(10000);
    await fixture.whenStable();
    // tick(10000);
    expect(cancelSpy).toHaveBeenCalledTimes(1);
  }));
});