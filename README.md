# Checklist First

A simple Android checklist app.

## Features

- Create multiple lists
- Add checklist items
- Check items off
- Progress count for each list
- Long-press an item to delete it
- Delete lists
- Data is stored locally on the device

## APK

Every push to `main` runs the GitHub Actions workflow in `.github/workflows/build-apk.yml` and publishes the debug APK as the `checklist-first-debug-apk` workflow artifact. The debug APK is installable on an Android device for testing.

The project uses Android Gradle Plugin 9.4.0 and Gradle 9.6.0. Android's current documentation lists AGP 9.4.0 as stable and Gradle 9.6.0 as its required/default Gradle version. 

## Build locally

Use Java 17 and Android SDK 36, then run:

```bash
gradle assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
