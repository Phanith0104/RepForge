# RepForge Implementation Plan

RepForge is a comprehensive fitness tracking application designed for step counting, gym workout logging with AI-enhanced visuals, and a structured calisthenics guide.

## User Review Required

> [!IMPORTANT]
> **AI Images for Exercises**: For the gym exercises, I will implement a system to display images. Since generating AI images in real-time requires a backend/API (like OpenAI DALL-E or Midjourney), I will initially use high-quality placeholders or a mock data set. We can integrate a specific API if you have one in mind.

> [!NOTE]
> **Step Counter Permissions**: Step counting requires `ACTIVITY_RECOGNITION` permission on Android 10+. The app will need to handle this permission request.

## Proposed Changes

### Project Setup
Initialize the Android project with modern best practices.
- **[NEW]** `build.gradle` (Project & App level)
- **[NEW]** `AndroidManifest.xml`
- **[NEW]** `MainActivity.kt`
- **[NEW]** Hilt setup for Dependency Injection

---

### Step Counter Module
Tracking daily steps and managing streaks.
- **[NEW]** `StepCounterService`: Foreground service to track steps in background.
- **[NEW]** `StepRepository`: Manages step data and streak logic (10k goal).
- **[NEW]** `StepViewModel` & `StepScreen`: UI to display daily progress and streaks.

---

### Gym Tracker Module
Logging manual sets and reps with exercise visuals.
- **[NEW]** `Exercise`: Data model including AI image URLs and alternative exercises.
- **[NEW]** `WorkoutSession`: Local DB storage for sets and reps.
- **[NEW]** `GymViewModel` & `GymTrackerScreen`: UI for manual entry and exercise details.

---

### Calisthenics Guide Module
Basics to advanced structured learning.
- **[NEW]** `CalisthenicsLevel`: Enum or Data class for progression levels.
- **[NEW]** `CalisthenicsScreen`: 2nd page with categorized exercises (Basics -> Advanced).

## Verification Plan

### Automated Tests
- **Unit Tests**: Test streak calculation logic (e.g., verify streak increments only at 10k).
- **Database Tests**: Verify Room persistence for workout logs.

### Manual Verification
- **Step Counting**: Verify sensor updates are reflected in the UI.
- **UI/UX**: Check navigation between the Step Counter, Gym Tracker, and Calisthenics pages.
- **Data Entry**: Manually enter sets/reps and ensure they are saved.
