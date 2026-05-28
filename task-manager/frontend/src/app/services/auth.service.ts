import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';

interface AuthResponse
{
        token: string;
        username: string;
}

const TOKEN_KEY = 'tm_auth_token';
const USER_KEY = 'tm_username';

@Injectable({ providedIn: 'root' })
export class AuthService
{
        private readonly authUrl = `${environment.apiBaseUrl}/auth`;

        constructor(private http: HttpClient) {}

        login(username: string, password: string): Observable<AuthResponse>
        {
                return this.http
                        .post<AuthResponse>(`${this.authUrl}/login`, { username, password })
                        .pipe(tap((res) => this.storeSession(res)));
        }

        register(username: string, password: string): Observable<AuthResponse>
        {
                return this.http
                        .post<AuthResponse>(`${this.authUrl}/register`, { username, password })
                        .pipe(tap((res) => this.storeSession(res)));
        }

        logout(): void
        {
                localStorage.removeItem(TOKEN_KEY);
                localStorage.removeItem(USER_KEY);
        }

        getToken(): string | null
        {
                return localStorage.getItem(TOKEN_KEY);
        }

        getUsername(): string | null
        {
                return localStorage.getItem(USER_KEY);
        }

        isLoggedIn(): boolean
        {
                return !!this.getToken();
        }

        private storeSession(res: AuthResponse): void
        {
                localStorage.setItem(TOKEN_KEY, res.token);
                localStorage.setItem(USER_KEY, res.username);
        }
}
