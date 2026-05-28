import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TaskService } from '../../services/task.service';
import { Task, TaskRequest, TaskStatus } from '../../models/task.model';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="create">
      <h2>New task</h2>
      <div class="row">
        <input class="grow" type="text" placeholder="What needs to be done?"
               [(ngModel)]="draft.title" (keyup.enter)="create()" />
        <select [(ngModel)]="draft.status">
          @for (status of statuses; track status) {
            <option [value]="status">{{ label(status) }}</option>
          }
        </select>
        <button class="primary" (click)="create()" [disabled]="!draft.title.trim()">Add</button>
      </div>
      <textarea class="desc" rows="2" placeholder="Description (optional)"
                [(ngModel)]="draft.description"></textarea>
      @if (createError) {
        <p class="error">{{ createError }}</p>
      }
    </section>

    <section class="list">
      <div class="list-head">
        <h2>Tasks</h2>
        <span class="count">{{ tasks.length }}</span>
      </div>

      @if (loading) {
        <p class="muted">Loading…</p>
      } @else if (loadError) {
        <p class="error">{{ loadError }}</p>
      } @else if (tasks.length === 0) {
        <p class="muted">No tasks yet — add your first one above.</p>
      } @else {
        @for (task of tasks; track task.id) {
          <article class="task" [class.done]="task.status === 'DONE'">
            @if (editingId === task.id) {
              <div class="edit">
                <input type="text" [(ngModel)]="editModel.title" />
                <textarea rows="2" [(ngModel)]="editModel.description"></textarea>
                <div class="edit-actions">
                  <button class="primary sm" (click)="saveEdit(task)">Save</button>
                  <button class="ghost sm" (click)="cancelEdit()">Cancel</button>
                </div>
              </div>
            } @else {
              <div class="task-main">
                <div class="task-text">
                  <h3>{{ task.title }}</h3>
                  @if (task.description) {
                    <p class="task-desc">{{ task.description }}</p>
                  }
                  <p class="task-meta">Created {{ task.createdAt | date:'MMM d, y, HH:mm' }}</p>
                </div>
                <span class="badge" [attr.data-status]="task.status">{{ label(task.status) }}</span>
              </div>
              <div class="task-actions">
                <select [value]="task.status"
                        (change)="changeStatus(task, asStatus($any($event.target).value))">
                  @for (status of statuses; track status) {
                    <option [value]="status">{{ label(status) }}</option>
                  }
                </select>
                <button class="ghost sm" (click)="startEdit(task)">Edit</button>
                <button class="danger sm" (click)="remove(task)">Delete</button>
              </div>
            }
          </article>
        }
      }
    </section>
  `,
  styles: [`
    h2 {
      font-family: var(--font-display);
      font-weight: 600;
      font-size: 1.2rem;
      margin: 0 0 16px;
    }
    .create {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 24px;
      margin-bottom: 32px;
    }
    .row {
      display: flex;
      gap: 10px;
      margin-bottom: 10px;
    }
    .grow { flex: 1; }
    input, select, textarea {
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 10px 12px;
      color: var(--text);
      font-size: 0.92rem;
      transition: border-color 0.15s ease;
    }
    input:focus, select:focus, textarea:focus {
      outline: none;
      border-color: var(--accent);
    }
    textarea { width: 100%; resize: vertical; }
    .desc { display: block; }
    select { cursor: pointer; }
    .list-head {
      display: flex;
      align-items: baseline;
      gap: 10px;
      margin-bottom: 16px;
    }
    .count {
      color: var(--text-muted);
      font-size: 0.85rem;
      background: var(--surface-2);
      border-radius: 20px;
      padding: 2px 11px;
    }
    .task {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 18px 20px;
      margin-bottom: 12px;
      transition: border-color 0.15s ease;
    }
    .task.done { opacity: 0.72; }
    .task.done h3 { text-decoration: line-through; }
    .task-main {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
    }
    h3 {
      margin: 0 0 4px;
      font-size: 1rem;
      font-weight: 600;
    }
    .task-desc {
      margin: 0 0 6px;
      color: var(--text-muted);
      font-size: 0.9rem;
    }
    .task-meta {
      margin: 0;
      color: var(--text-muted);
      font-size: 0.76rem;
    }
    .badge {
      flex-shrink: 0;
      font-size: 0.72rem;
      font-weight: 600;
      padding: 4px 11px;
      border-radius: 20px;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .badge[data-status='TODO'] { background: var(--surface-2); color: var(--todo); }
    .badge[data-status='IN_PROGRESS'] { background: rgba(79, 155, 214, 0.15); color: var(--progress); }
    .badge[data-status='DONE'] { background: rgba(111, 191, 115, 0.15); color: var(--done); }
    .task-actions, .edit-actions {
      display: flex;
      gap: 8px;
      margin-top: 14px;
      align-items: center;
    }
    .edit { display: flex; flex-direction: column; gap: 10px; }
    .edit input, .edit textarea { width: 100%; }
    button { border-radius: 8px; transition: all 0.15s ease; }
    .sm { padding: 7px 14px; font-size: 0.82rem; }
    .primary {
      background: var(--accent);
      color: #1a1408;
      border: none;
      padding: 10px 18px;
      font-weight: 600;
    }
    .primary:hover:not(:disabled) { filter: brightness(1.08); }
    .primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .ghost {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text-muted);
    }
    .ghost:hover { border-color: var(--accent); color: var(--accent); }
    .danger {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--danger);
    }
    .danger:hover { border-color: var(--danger); background: rgba(214, 105, 79, 0.1); }
    .muted { color: var(--text-muted); }
    .error { color: var(--danger); font-size: 0.85rem; margin: 8px 0 0; }
  `]
})
export class TaskListComponent implements OnInit {

  tasks: Task[] = [];
  loading = false;
  loadError = '';
  createError = '';

  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

  draft: TaskRequest = { title: '', description: '', status: 'TODO' };

  editingId: number | null = null;
  editModel: TaskRequest = { title: '', description: '', status: 'TODO' };

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.loadError = '';
    this.taskService.getAll().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: () => {
        this.loadError = 'Could not load tasks. Is the backend running?';
        this.loading = false;
      }
    });
  }

  create(): void {
    if (!this.draft.title.trim()) {
      return;
    }
    this.createError = '';
    this.taskService.create(this.draft).subscribe({
      next: (task) => {
        this.tasks = [...this.tasks, task];
        this.draft = { title: '', description: '', status: 'TODO' };
      },
      error: (err) => {
        this.createError = err?.error?.message || 'Could not create the task.';
      }
    });
  }

  changeStatus(task: Task, status: TaskStatus): void {
    const request: TaskRequest = {
      title: task.title,
      description: task.description,
      status
    };
    this.taskService.update(task.id, request).subscribe({
      next: (updated) => this.replace(updated),
      error: () => this.load()
    });
  }

  startEdit(task: Task): void {
    this.editingId = task.id;
    this.editModel = {
      title: task.title,
      description: task.description ?? '',
      status: task.status
    };
  }

  cancelEdit(): void {
    this.editingId = null;
  }

  saveEdit(task: Task): void {
    this.taskService.update(task.id, this.editModel).subscribe({
      next: (updated) => {
        this.replace(updated);
        this.editingId = null;
      },
      error: () => this.load()
    });
  }

  remove(task: Task): void {
    this.taskService.delete(task.id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter((t) => t.id !== task.id);
      },
      error: () => this.load()
    });
  }

  label(status: TaskStatus): string {
    switch (status) {
      case 'TODO': return 'To do';
      case 'IN_PROGRESS': return 'In progress';
      case 'DONE': return 'Done';
    }
  }

  asStatus(value: string): TaskStatus {
    return value as TaskStatus;
  }

  private replace(updated: Task): void {
    this.tasks = this.tasks.map((t) => (t.id === updated.id ? updated : t));
  }
}
