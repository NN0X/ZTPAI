import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="card">
      <p class="eyebrow">{{ mode === 'login' ? 'Welcome back' : 'Get started' }}</p>
      <h2>{{ mode === 'login' ? 'Sign in to continue' : 'Create an account' }}</h2>

      <div class="field">
        <label for="username">Username</label>
        <input id="username" type="text" [(ngModel)]="username" autocomplete="username"
               (keyup.enter)="submit()" />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" type="password" [(ngModel)]="password" autocomplete="current-password"
               (keyup.enter)="submit()" placeholder="••••••••" />
      </div>

      @if (error) {
        <p class="error">{{ error }}</p>
      }

      <button class="primary" (click)="submit()" [disabled]="loading">
        {{ loading ? 'Please wait…' : (mode === 'login' ? 'Sign in' : 'Register') }}
      </button>

      <p class="switch">
        {{ mode === 'login' ? "No account yet?" : 'Already have an account?' }}
        <a (click)="toggleMode()">{{ mode === 'login' ? 'Register' : 'Sign in' }}</a>
      </p>
    </div>
  `,
  styles: [`
    .card {
      max-width: 400px;
      margin: 40px auto 0;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 36px 32px;
      box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
    }
    .eyebrow {
      text-transform: uppercase;
      letter-spacing: 0.14em;
      font-size: 0.7rem;
      color: var(--accent);
      margin: 0 0 6px;
    }
    h2 {
      font-family: var(--font-display);
      font-weight: 600;
      font-size: 1.5rem;
      margin: 0 0 26px;
    }
    .field {
      margin-bottom: 18px;
    }
    label {
      display: block;
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-bottom: 7px;
    }
    input {
      width: 100%;
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 11px 13px;
      color: var(--text);
      font-size: 0.95rem;
      transition: border-color 0.15s ease;
    }
    input:focus {
      outline: none;
      border-color: var(--accent);
    }
    .error {
      color: var(--danger);
      font-size: 0.85rem;
      margin: 0 0 16px;
    }
    .primary {
      width: 100%;
      background: var(--accent);
      color: #1a1408;
      border: none;
      border-radius: 8px;
      padding: 12px;
      font-weight: 600;
      font-size: 0.95rem;
      margin-top: 4px;
      transition: filter 0.15s ease;
    }
    .primary:hover:not(:disabled) {
      filter: brightness(1.08);
    }
    .primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .switch {
      text-align: center;
      color: var(--text-muted);
      font-size: 0.85rem;
      margin: 22px 0 0;
    }
    .switch a {
      color: var(--accent);
      cursor: pointer;
      font-weight: 600;
    }
  `]
})
export class LoginComponent
{
        username = '';
        password = '';
        error = '';
        loading = false;
        mode: 'login' | 'register' = 'login';

        constructor(private auth: AuthService, private router: Router) {}

        toggleMode(): void
        {
                this.mode = this.mode === 'login' ? 'register' : 'login';
                this.error = '';
        }

        submit(): void
        {
                if (!this.username || !this.password)
                {
                        this.error = 'Please enter a username and password.';
                        return;
                }

                this.error = '';
                this.loading = true;

                const request$ = this.mode === 'login'
                        ? this.auth.login(this.username, this.password)
                        : this.auth.register(this.username, this.password);

                request$.subscribe({
                        next: () =>
                        {
                                this.loading = false;
                                this.router.navigate(['/tasks']);
                        },
                        error: (err) =>
                        {
                                this.loading = false;
                                this.error = err?.error?.message || 'Authentication failed. Please try again.';
                        }
                });
        }
}
