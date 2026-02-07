# PeerStack

PeerStack is a social media platform for developers where users can create profiles, share updates, and connect with other programmers. This Session 1 implementation delivers the core registration and authentication flow through a Spring Boot backend and a React web application.

## Technologies Used

- Backend: Java 17, Spring Boot 3, Spring Web, Spring Validation, BCrypt (`spring-security-crypto`)
- Web: React 18, React Router, Vite
- Data Store (Session 1): In-memory storage (no database required for now)
- Planned later: MySQL integration and mobile app implementation

## Project Structure

```text
PeerStack
├─ /web
├─ /backend
├─ /mobile
├─ /docs
├─ README.md
└─ TASK_CHECKLIST.md
```

## Implemented Scope (Session 1)

### Backend (Spring Boot)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/user/me` (protected)
- Password encryption with BCrypt
- Token-based authentication using local memory

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
   mvn spring-boot:run
   ```
3. Backend runs on:
   ```text
   http://localhost:8080
   ```

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
- `/mobile` is a placeholder folder.
- Mobile development will be added in Session 2.

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

- Register, Login, Dashboard, Profile, Logout
- Create posts (text + optional code snippet)
- Like and comment on posts
- Add friends / accept friend requests
- Transactional function: send and accept friend requests

## Note on MySQL

Per Session 1 constraints for this environment, the current implementation uses in-memory storage and does not require local database/admin setup.
MySQL integration can be added in a future session.
