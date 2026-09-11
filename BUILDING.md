# Building AndroidVSCode

## Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Use JDK 17.
4. Allow Gradle sync to complete.
5. Select the `app` configuration.
6. Run on Android 8.0+ (API 26+) or a newer device.

## Command line

If Gradle is installed:

```
gradle assembleDebug
```

The debug APK is generated under:

```
app/build/outputs/apk/debug/app-debug.apk
```
