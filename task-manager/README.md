# Task Manager — Spring Boot REST API + Angular

A small full-stack task management application built to satisfy the **5.0 (Bardzo dobry)** grading criteria:
a layered Spring Boot REST API with JWT security, validation, error handling, domain events and unit tests,
plus a simple Angular frontend that consumes the API.

---

## Tech stack

| Layer      | Technology                                              |
|------------|---------------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.4, Spring Web, Spring Data JPA   |
| Security   | Spring Security + JWT (JJWT 0.12)                       |
| Database   | H2 (in-memory) via Hibernate                            |
| Validation | Jakarta Bean Validation                                 |
| Events     | Spring Application Events (`ApplicationEventPublisher`) |
| Tests      | JUnit 5, Mockito, AssertJ                               |
| Frontend   | Angular 17 (standalone components)                      |
| Build      | Maven (backend), npm / Angular CLI (frontend)           |

---

## Prerequisites

- **JDK 17+** (the project targets Java 17; it also compiles and runs on 21)
- **Maven 3.9+** — or use your IDE's bundled Maven
- **Node.js 18+** and **npm** (only needed for the frontend)

---

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

On startup the app seeds:
- a default user — **username:** `admin`, **password:** `admin123`
- two sample tasks

H2 console (optional, for inspecting data) is available at
**http://localhost:8080/h2-console** with JDBC URL `jdbc:h2:mem:taskdb`, user `sa`, empty password.

### Run the tests

```bash
cd backend
mvn test
```

---

## Running the frontend

```bash
cd frontend
npm install
npm start
```

The app is served on **http://localhost:4200** and is pre-configured to call the API at
`http://localhost:8080/api` (see `src/environments/environment.ts`). CORS for `localhost:4200`
is already enabled on the backend.

Log in with `admin` / `admin123`, or register a new account.

---

## API reference

All `/api/tasks` endpoints require a `Authorization: Bearer <token>` header.

### Auth (public)

| Method | Path                 | Body                          | Description                |
|--------|----------------------|-------------------------------|----------------------------|
| POST   | `/api/auth/register` | `{ "username", "password" }`  | Create user, returns token |
| POST   | `/api/auth/login`    | `{ "username", "password" }`  | Authenticate, returns token |

Response: `{ "token": "...", "username": "..." }`

### Tasks (authenticated)

| Method | Path              | Body          | Description           |
|--------|-------------------|---------------|-----------------------|
| GET    | `/api/tasks`      | —             | List all tasks        |
| GET    | `/api/tasks/{id}` | —             | Get one task          |
| POST   | `/api/tasks`      | `TaskRequest` | Create a task (201)   |
| PUT    | `/api/tasks/{id}` | `TaskRequest` | Update a task         |
| DELETE | `/api/tasks/{id}` | —             | Delete a task (204)   |

`TaskRequest`:

```json
{
  "title": "Write the report",
  "description": "optional, up to 1000 chars",
  "status": "TODO"
}
```

`status` is one of `TODO`, `IN_PROGRESS`, `DONE`. Moving a task to `DONE` sets `completedAt`
and publishes a `TaskCompletedEvent`.

### Quick test with curl

```bash
# 1. Log in and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# 2. Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Try the API","status":"TODO"}'

# 3. List tasks
curl http://localhost:8080/api/tasks -H "Authorization: Bearer $TOKEN"
```

---

## Project structure

```
task-manager/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/taskmanager/
│       │   ├── config/        SecurityConfig, DataInitializer
│       │   ├── controller/    TaskController, AuthController
│       │   ├── service/       TaskService, AuthService
│       │   ├── repository/    TaskRepository, UserRepository
│       │   ├── domain/        Task, AppUser, TaskStatus, Role
│       │   ├── dto/           request/response records
│       │   ├── mapper/        TaskMapper
│       │   ├── event/         events + listener
│       │   ├── security/      JwtService, JwtAuthenticationFilter, …
│       │   └── exception/     GlobalExceptionHandler, ApiError, …
│       ├── main/resources/    application.yml
│       └── test/java/...      TaskServiceTest, JwtServiceTest
└── frontend/
    └── src/app/
        ├── components/        login, tasks
        ├── services/          auth.service, task.service
        ├── interceptors/      auth.interceptor (attaches JWT)
        ├── guards/            auth.guard
        └── models/            task.model
```

---

## Design choices & notes

- **H2 in-memory** keeps setup to zero for grading; data resets on restart. To use PostgreSQL,
  swap the `spring.datasource.*` properties in `application.yml` and add the Postgres driver to `pom.xml`.
- **JWT secret** lives in `application.yml` for convenience. In a real deployment, override it via the
  `APP_JWT_SECRET` environment variable and never commit a production secret.
- The frontend stores the JWT in `localStorage` and attaches it through an HTTP interceptor.
- This is intentionally the **minimum scope** for a 5.0 grade — one entity, lean but complete.
```
