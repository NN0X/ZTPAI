import { Component } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';

import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="shell">
      <header class="topbar">
        <div class="brand">
          <span class="dot"></span>
          <h1>Task<span>Manager</span></h1>
        </div>
        @if (auth.isLoggedIn()) {
          <div class="session">
            <span class="user">{{ auth.getUsername() }}</span>
            <button class="ghost" (click)="logout()">Sign out</button>
          </div>
        }
      </header>
      <main class="content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .shell {
      max-width: 880px;
      margin: 0 auto;
      padding: 0 20px 64px;
    }
    .topbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 28px 0 32px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: var(--accent);
      box-shadow: 0 0 16px var(--accent);
    }
    h1 {
      font-family: var(--font-display);
      font-weight: 600;
      font-size: 1.5rem;
      letter-spacing: -0.01em;
      margin: 0;
      color: var(--text);
    }
    h1 span {
      color: var(--text-muted);
      font-style: italic;
    }
    .session {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .user {
      color: var(--text-muted);
      font-size: 0.9rem;
    }
    .ghost {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 8px 16px;
      border-radius: 8px;
      font-size: 0.85rem;
      transition: all 0.15s ease;
    }
    .ghost:hover {
      border-color: var(--accent);
      color: var(--accent);
    }
  `]
})
export class AppComponent
{
        constructor(public auth: AuthService, private router: Router) {}

        logout(): void
        {
                this.auth.logout();
                this.router.navigate(['/login']);
        }
}
