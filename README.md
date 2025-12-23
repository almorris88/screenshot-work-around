# Screenshot Workaround

A minimal Android app that captures screenshots even when blocked by app permissions.

## What This Does

This app uses Android's MediaProjection API to capture screenshots, bypassing restrictions that some apps impose using `FLAG_SECURE`. This is useful for:
- Taking screenshots in banking apps
- Capturing content from DRM-protected apps
- Personal archiving and note-taking

## How It Works

The app uses the legitimate MediaProjection API which requires explicit user permission each time. When you tap "Capture Screenshot", Android will prompt you to allow screen recording, then the app captures a single frame and saves it to your Pictures/Screenshots folder.

## Building the App

### Prerequisites
- Android Studio (latest version)
- JDK 8 or higher
- Android SDK (API level 21+)

### Build Steps

1. Clone this repository:
   ```bash
   git clone <repo-url>
   cd screenshot-work-around
   ```

2. Open in Android Studio or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

3. The APK will be in: `app/build/outputs/apk/debug/app-debug.apk`

4. Install on your device:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Usage

1. Open the app
2. Tap "Capture Screenshot"
3. Grant permission when prompted
4. Screenshot is saved to Pictures/Screenshots/

## Technical Details

- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Size**: ~50KB (minimal dependencies)

## Permissions Required

- `FOREGROUND_SERVICE` - For MediaProjection
- `WRITE_EXTERNAL_STORAGE` - For saving screenshots (Android ≤9)
- `READ_EXTERNAL_STORAGE` - For reading storage (Android ≤12)

## Important Notes

- This is for **personal use only**
- Respect copyright and privacy laws
- Each screenshot requires user permission
- Works on Android 5.0 and above

## Project Structure

```
app/
├── build.gradle
└── src/main/
    ├── AndroidManifest.xml
    ├── java/com/screenshot/workaround/
    │   └── MainActivity.kt
    └── res/
        └── values/
            └── strings.xml
```

## Credits

Created with assistance from Claude AI to help users capture screenshots for legitimate personal use.
