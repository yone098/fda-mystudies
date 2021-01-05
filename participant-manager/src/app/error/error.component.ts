import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ErrorCode, getMessage} from '../shared/error.codes.enum';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss'],
})
export class ErrorComponent implements OnInit {
  errorCode = '';
  errorMessage = '';

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params['errorCode']) {
        this.errorCode = params.errorCode as string;
      }
      this.errorMessage = getMessage(this.errorCode as ErrorCode);
    });
  }
}
