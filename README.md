# iMessage Local

Offline Android messenger (Kotlin, Jetpack Compose, Room, MVVM). No INTERNET permission, no accounts, no ads.

## Build
1. Open this folder in Android Studio (Ladybug or newer).
2. Let Gradle sync.
3. Build > Build APK(s) for a debug install, or Build > Generate Signed APK for a release build.

## Layout
- `data/`: Room entities, DAO, database, repository, preferences.
- `ui/`: Compose screens, theme, and the shared `AppViewModel`.
- `Notifier.kt`: local notifications with a sound and a silent channel.

## Not included yet
- Reading contacts from the phone book, photo/file attachments, backup and restore, real SMS.


## Build APK with GitHub Actions from a phone

1. Push this project to a GitHub repository on branch `main` or `master`.
2. Open the repository's **Actions** tab.
3. Select **Build Android APK** and run it with **Run workflow**, or push a commit.
4. Open the completed workflow run and download the `iMessageLocal-debug-apk` artifact.
5. Extract the ZIP artifact to get `app-debug.apk`.

The workflow uses JDK 17 and Gradle 8.7. No Gradle Wrapper is required.
