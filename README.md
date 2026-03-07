# CollabMatch

CollabMatch is a platform where users can find partners or teammates for projects by posting collaboration opportunities and joining teams based on skills and interests. It helps people connect easily and work together on creative or academic projects.

## Target Users

- Students working on group projects
- Creators (artists, editors, writers, designers)
- People looking for teammates for events or competitions

## Technologies Used

- Backend: Java 17, Spring Boot 3, Spring Web, Spring Validation, BCrypt (`spring-security-crypto`)
- Web: React 18, React Router, Vite
- Mobile: Android Kotlin, Jetpack Compose, Retrofit
- Data Store: PostgreSQL (Flyway migrations + Spring Data JPA)

## Project Structure

```text
CollabMatch
|-- /web
|-- /backend
|-- /mobile
|-- /docs
|-- README.md
`-- TASK_CHECKLIST.md
```

## Implemented Scope

### Backend (Spring Boot)
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/user/me` (protected)
- Password encryption with BCrypt
- JWT access tokens + refresh tokens (stored in DB)

### Web App (React)
- Register page
- Login page
- Dashboard/Profile page (protected)
- Logout functionality

### Mobile App (Android Kotlin)
- Register screen
- Login screen
- Dashboard/Profile screen (protected)
- Logout functionality
- Backend integration via Retrofit

## Steps to Run Backend

1. Create the PostgreSQL database and user:
   ```sql
   CREATE USER collabmatch WITH PASSWORD 'collabmatch';
   CREATE DATABASE collabmatch OWNER collabmatch;
   ```
2. Open terminal in `backend`:
   ```bash
   cd backend
   ```
3. Run the app:
   ```bash
   mvnw.cmd spring-boot:run
   ```
4. Backend runs on:
   ```text
   http://localhost:8080
   ```

Prerequisite:
- `JAVA_HOME` must point to a Java 17 JDK directory.

## Steps to Run Web App

1. Open terminal in `web`:
   ```bash
   cd web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start development server:
   ```bash
   npm run dev
   ```
4. Web app runs on:
   ```text
   http://localhost:5173
   ```

## Steps to Run Mobile App

1. Open `mobile/` in Android Studio.
2. Wait for Gradle sync.
3. Run Android emulator.
4. Make sure backend is running on `http://localhost:8080`.
5. Launch app.

Note:
- Emulator uses `http://10.0.2.2:8080` to access host backend.
- Flyway will auto-create the `users` table on first run.

## API Endpoints

### 1) Register
- Method: `POST`
- URL: `/api/v1/auth/register`
- Request body:
  ```json
  {
    "email": "john@example.com",
    "password": "secret12345",
    "firstname": "John",
    "lastname": "Doe"
  }
  ```
- Success response: `201 Created`

### 2) Login
- Method: `POST`
- URL: `/api/v1/auth/login`
- Request body:
  ```json
  {
    "email": "john@example.com",
    "password": "secret12345"
  }
  ```
- Success response: `200 OK`

### 3) Current User (Protected)
- Method: `GET`
- URL: `/api/v1/user/me`
- Header:
  ```text
  Authorization: Bearer <accessToken>
  ```
- Success response: `200 OK`

### 4) Logout
- Method: `POST`
- URL: `/api/v1/auth/logout`
- Headers:
  ```text
  Authorization: Bearer <accessToken>
  ```
- Request body:
  ```json
  {
    "refreshToken": "<refreshToken>"
  }
  ```
- Success response: `200 OK`

### Response Format

All API endpoints return a consistent response envelope (aligned to the SDD):

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-02-14T04:00:00Z"
}
```

## Main Functions / Features (Project Vision)

- Register, login, dashboard/profile, logout
- Post collaboration opportunities for projects or events
- Discover teammates based on skills and interests
- Join teams and manage collaboration requests
- Build project groups for creative and academic work

## Note on PostgreSQL

The backend now uses PostgreSQL with Flyway migrations and Spring Data JPA for persistence.
