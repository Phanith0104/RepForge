# RepForge Application Audit and Fix Plan

This plan addresses all issues identified in the RepForge/GymTracker application, focusing on reliability, security, and user experience.

## User Review Required

> [!IMPORTANT]
> **Database Migrations:** I will remove `fallbackToDestructiveMigration(true)` to protect user data. If schema changes are necessary, I will implement explicit Room migrations.
> **Authentication Flow:** The `loginWithEmail` method currently auto-registers users. I will separate login and registration if required, or at least improve the messaging to make it clear.
> **Foreground Service:** Starting a foreground service from `BootReceiver` is restricted on newer Android versions. I will implement a more robust start mechanism, potentially using a transparent Activity or ensuring it only starts when the app is in a valid state.

## Proposed Changes

### 1. Authentication & Login Flow
Fix the login screen and underlying authentication logic.

#### [MODIFY] [LoginScreen.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/ui/auth/LoginScreen.kt)
- Improve UI responsiveness and modern styling.
- Add proper email validation and error display.
- Fix keyboard handling and password visibility stability.
- Handle all authentication states (Success, Error, Loading, Canceled).
- Implement "Forgot Password" validation and success messaging.
- Fix Google Sign-in button functionality and cancellation handling.

#### [MODIFY] [AuthViewModel.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/ui/auth/AuthViewModel.kt)
- Ensure `isLoading` is consistently reset.
- Improve error mapping for user-friendly messages.
- Clear user-specific states upon logout.

#### [MODIFY] [UserRepository.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/data/repository/UserRepository.kt)
- Fix session recovery and sync logic.
- Improve Firestore error handling and permissions logging.

---

### 2. Step Tracker & Streak Logic
Implement the requested consecutive-date streak calculation and improve background service reliability.

#### [MODIFY] [StepRepository.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/data/repository/StepRepository.kt)
- Implement `calculateCurrentStreak()` to iterate backwards from today/yesterday through consecutive qualifying days.
- Ensure streak is recalculated on all relevant data mutations (updates, deletes).
- Use `getCurrentUserId()` for all operations.

#### [MODIFY] [StepCounterService.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/stepcounter/StepCounterService.kt)
- Optimize sensor delay for battery efficiency.
- Handle service start exceptions.
- Improve notification updates with more detail.

#### [MODIFY] [BootReceiver.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/stepcounter/BootReceiver.kt)
- Wrap service start in additional safety checks for Android 12+ restrictions.

---

### 3. Workout History & User Data Isolation
Ensure all workout data is strictly isolated by user ID and correctly managed.

#### [MODIFY] [GymViewModel.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/ui/gym/GymViewModel.kt)
- Stop passing empty `userId` to repository calls.
- Improve workout deletion logic to refresh UI immediately.
- Add validation for template JSON data.

#### [MODIFY] [WorkoutDao.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/data/local/dao/WorkoutDao.kt)
- Verify and improve queries to ensure strict `userId` filtering.

---

### 4. Permission Handling
Robust runtime permission requests in `MainActivity`.

#### [MODIFY] [MainActivity.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/MainActivity.kt)
- Handle "Permanently Denied" state by providing a path to app settings.
- Ensure `POST_NOTIFICATIONS` is requested correctly on Android 13+.

---

### 5. Database & State Management
Secure data persistence and ensure UI consistency.

#### [MODIFY] [DatabaseModule.kt](file:///D:/Projects/RepForge/app/src/main/kotlin/com/repforge/app/src/main/kotlin/com/repforge/di/DatabaseModule.kt)
- Remove destructive migrations.

## Verification Plan

### Automated Tests
- Unit tests for `StepRepository` streak calculation (3 days, 2 days with gap, PB logic).
- Mock authentication tests for `AuthViewModel` state transitions.
- DAO tests for user data isolation.

### Manual Verification
- Perform login/logout with multiple accounts and verify data isolation.
- Test step counter service behavior on reboot and sensor absence.
- Manually edit and delete workout history records and verify UI updates.
- Test permission denial and re-granting flows.
