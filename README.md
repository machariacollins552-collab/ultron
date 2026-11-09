# Kai — Executive AI Assistant

Kai is an Android-first executive assistant starter built with Kotlin and Jetpack Compose.

## Included in this first published MVP
- Light Muse-inspired dashboard and bottom navigation.
- Chat screen connected to a provider selected in Settings.
- AI employee team directory with defined roles.
- Settings for Groq, Gemini, and OpenAI API credentials.
- API keys encrypted locally with Android Keystore (AES/GCM); keys are never stored in source code or GitHub.
- Test connection action and provider/model selection.
- GitHub Actions workflow that builds a debug APK and uploads it as an artifact.

## Build locally
1. Install Android Studio (Ladybug or newer) and Android SDK 35.
2. Open this repository in Android Studio.
3. Let Gradle sync, then run the `app` configuration on an Android device or emulator.

Command line (with Android SDK installed):
```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Build APK on GitHub
Open **Actions → Build Kai Android APK → Run workflow**. Download the `kai-debug-apk` artifact after the workflow completes.

## Add your API key
Open Kai → Settings → choose Groq, Gemini, or OpenAI → enter your own key → Save key → Test connection. Do not paste API keys into GitHub, source files, issues, or chat messages.

Provider free tiers and model availability are controlled by each provider and can change. Kai does not provide free API credits.

## Security notes
- API credentials are encrypted using a non-exportable Android Keystore AES key and stored encrypted in app-private preferences.
- The app requests Internet access only.
- No server database is required for this MVP.
- Clear app data/uninstalling removes the locally stored credential.
- This is an early MVP. WhatsApp, email, calendar, voice, multimodal file handling, offline task queue, and background notifications are planned integrations, not claimed as implemented in this release.

## Project
- Package: `com.kai.assistant`
- Min Android: 8.0 (API 26)
- Target/compile SDK: 35
- Language: Kotlin
- UI: Jetpack Compose
