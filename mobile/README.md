# CollabMatch Mobile App (Android Kotlin)

This module now includes:
- Register screen
- Login screen
- Dashboard/Profile screen (protected)
- Logout functionality
- Integration with the same Spring Boot backend

## Tech Stack
- Kotlin
- Jetpack Compose
- Retrofit + OkHttp
- SharedPreferences for token persistence

## Important Backend URL Note
- Android emulator cannot access host localhost directly.
- The app uses `http://10.0.2.2:8080/` as backend base URL.
- Keep your Spring Boot backend running on port `8080` on your host machine.

## How to Run
1. Open `mobile/` in Android Studio.
2. Let Gradle sync complete.
3. Run an emulator.
4. Start backend:
   - `cd backend`
   - `mvnw.cmd spring-boot:run`
5. Run the Android app.

## Requirements
- JDK 17 for Android Gradle Plugin
