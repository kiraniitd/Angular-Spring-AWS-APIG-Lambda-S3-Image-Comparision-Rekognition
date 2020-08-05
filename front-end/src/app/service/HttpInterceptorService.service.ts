import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthenticationService } from './authentication.service';


@Injectable()
export class HttpInterceptorService implements HttpInterceptor {
    constructor(private authenticationService: AuthenticationService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        console.log('Intercepted URL-->',request.url);
        return next.handle(request).pipe(catchError(err => {
            console.log ('Error Status is -->', err.status);
            console.log ('Error Message is -->', err.message);
            if (err.status === 401 || err.status === 404){
                console.log ('In 401 and 404 loop');
            }
            if (err.status === 403) {
                console.log ('In 403 loop');
            }
            if (err.status === 500) {
                console.log ('In 500 loop');
           }
            return throwError(err.status);
        }))
    }
}