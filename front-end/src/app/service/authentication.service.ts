import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

constructor(private http: HttpClient, private router: Router ) { }

 public getImageComparisionData(getDataFromS3){
  const response = this.http.get('https://wsrramyk64.execute-api.us-east-1.amazonaws.com/v1/info', getDataFromS3);
   // const response = this.http.get('http://localhost:8080/v1/info/', getDataFromS3);
   // const response = this.http.get('https://wsrramyk64.execute-api.us-east-1.amazonaws.com/default/aws-image-rekognition-demo', getDataFromS3);   
  console.log('Response is -Image Data->',  JSON.stringify(response));
  return response;
  }


}
