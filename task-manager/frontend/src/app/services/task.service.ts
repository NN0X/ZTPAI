import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Task, TaskRequest } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService
{
        private readonly tasksUrl = `${environment.apiBaseUrl}/tasks`;

        constructor(private http: HttpClient) {}

        getAll(): Observable<Task[]>
        {
                return this.http.get<Task[]>(this.tasksUrl);
        }

        create(request: TaskRequest): Observable<Task>
        {
                return this.http.post<Task>(this.tasksUrl, request);
        }

        update(id: number, request: TaskRequest): Observable<Task>
        {
                return this.http.put<Task>(`${this.tasksUrl}/${id}`, request);
        }

        delete(id: number): Observable<void>
        {
                return this.http.delete<void>(`${this.tasksUrl}/${id}`);
        }
}
