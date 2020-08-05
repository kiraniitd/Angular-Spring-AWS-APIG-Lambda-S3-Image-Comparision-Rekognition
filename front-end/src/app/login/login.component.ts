import { Component, OnInit, Input, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../service/authentication.service';
import { FormGroup, FormBuilder, FormControl, Validators} from '@angular/forms';
import JSONFormatter from 'json-formatter-js';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {


  message: any;
  errorMessage: any;
  loginForm: FormGroup;
  getDataFromS3: any;
  payLoad: any;
  payLoadJSON: any;

  constructor(private service: AuthenticationService, private router: Router, private formBuilder: FormBuilder, private element: ElementRef) { }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({

    });
 
  }
  
  getImageData() {
    console.log('In getImageMatchingData');
    this.getDataFromS3 = {};

    const resp = this.service.getImageComparisionData(this.getDataFromS3);
    console.log('resp is -->', resp);
    resp.subscribe(data => {
      console.log('Data is -->',  JSON.stringify(data));
      this.message = data;
      console.log('Message is -->', this.message);
      this.payLoad = (JSON.stringify(this.message).split('[').join('\n')).split(',').join('\n');
    }, error => {
      this.errorMessage = error;
    });
  }


}

