# Uzzap

Uzzap is an Android instant-messaging app built with Kotlin and Jetpack Compose. It includes buddy lists, direct chats, Philippine regional chatrooms, profiles, presence controls, and messaging preferences.

## Tech stack

- Kotlin and Jetpack Compose with Material 3
- Room for local persistence
- Firebase Authentication and Cloud Firestore
- Gradle 9.3.1 and Android Gradle Plugin 9.1.1
- Robolectric, Compose UI Test, and Roborazzi for testing

## Requirements

- JDK 21
- Android SDK Platform 36
- Android Build Tools 36.x
- Gradle 9.3.1, or Android Studio with an equivalent Gradle setup
- An Android device or emulator running API 24 or later

The repository contains Gradle wrapper properties but does not currently include the `gradlew` launcher or wrapper JAR. Use a locally installed Gradle 9.3.1 until those files are restored.

## Firebase setup

Firebase-backed sign-in and messaging require an Android Firebase project:

1. Register the application ID `com.aistudio.uzzap.kxvtpm` in Firebase.
2. Download that project's `google-services.json`.
3. Place it at `app/google-services.json`.
4. Enable Authentication and Cloud Firestore for the project.

Do not commit `google-services.json` or production credentials. The debug APK can compile without the file, but Firebase features will not work correctly at runtime.

## Build

From the repository root:

```bash
gradle :app:assembleDebug
```

The resulting APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

If the build host has limited memory, use a bounded invocation:

```bash
gradle --no-daemon --no-configuration-cache \
  -Dorg.gradle.jvmargs='-Xmx2g -Dfile.encoding=UTF-8' \
  -Dorg.gradle.workers.max=1 \
  -Dorg.gradle.parallel=false \
  :app:assembleDebug
```

## Test

Run the host-side unit and Robolectric tests with:

```bash
gradle --no-configuration-cache :app:testDebugUnitTest
```

Instrumentation tests require a connected Android device or emulator:

```bash
gradle :app:connectedDebugAndroidTest
```

## Project structure

```text
app/src/main/java/com/example/
├── auth/           Authentication integration
├── data/           Local models, Room storage, and Firestore services
├── ui/components/  Shared Compose components
├── ui/screens/     Application screens
├── ui/theme/       Colors, typography, and app theme
├── ui/viewmodel/   UI state and application actions
└── MainActivity.kt Application entry point and top-level navigation
```

Firestore security rules and indexes are stored in `firestore.rules` and `firestore.indexes.json`.
