# CollabMatch

CollabMatch is a platform where users can find partners or teammates for projects by posting collaboration opportunities and joining teams based on skills and interests. It helps people connect easily and work together on creative or academic projects.

## Target Users

- Students working on group projects
- Creators (artists, editors, writers, designers)
- People looking for teammates for events or competitions

## Technologies Used

- Backend: Java 17, Spring Boot 3, Spring Web, Spring Validation, BCrypt (`spring-security-crypto`)
- Web: React 18, React Router, Vite
- Data Store (Session 1): In-memory storage (no database required for now)
- Planned later: MySQL integration and mobile app implementation

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

## Implemented Scope (Session 1)

### Backend (Spring Boot)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/user/me` (protected)
- Password encryption with BCrypt
- Token-based authentication using in-memory storage

### Web App (React)
- Register page
- Login page
- Dashboard/Profile page (protected)
- Logout functionality

## Steps to Run Backend

1. Open terminal in `backend`:
   ```bash
   cd backend
   ```
2. Run the app:
   ```bash
   mvnw.cmd spring-boot:run
   ```
3. Backend runs on:
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

Mobile app is intentionally not implemented in Session 1.

Current status:
- `/mobile` is a placeholder folder
- Mobile development will be added in Session 2

## API Endpoints

### 1) Register
- Method: `POST`
- URL: `/api/auth/register`
- Request body:
  ```json
  {
    "username": "johndoe",
    "email": "john@example.com",
    "password": "secret123"
  }
  ```
- Success response: `201 Created`

### 2) Login
- Method: `POST`
- URL: `/api/auth/login`
- Request body:
  ```json
  {
    "username": "johndoe",
    "password": "secret123"
  }
  ```
- Success response: `200 OK`

### 3) Current User (Protected)
- Method: `GET`
- URL: `/api/user/me`
- Header:
  ```text
  Authorization: Bearer <token>
  ```
- Success response: `200 OK`

### 4) Logout
- Method: `POST`
- URL: `/api/auth/logout`
- Header:
  ```text
  Authorization: Bearer <token>
  ```
- Success response: `204 No Content`

## Main Functions / Features (Project Vision)

- Register, login, dashboard/profile, logout
- Post collaboration opportunities for projects or events
- Discover teammates based on skills and interests
- Join teams and manage collaboration requests
- Build project groups for creative and academic work

## Note on MySQL

Per Session 1 constraints for this environment, the current implementation uses in-memory storage and does not require local database/admin setup.
MySQL integration can be added in a future session.
