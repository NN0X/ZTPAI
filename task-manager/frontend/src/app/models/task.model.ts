export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task
{
        id: number;
        title: string;
        description?: string;
        status: TaskStatus;
        createdAt: string;
        completedAt?: string;
}

export interface TaskRequest
{
        title: string;
        description?: string;
        status: TaskStatus;
}
