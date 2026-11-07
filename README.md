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


## Real SMS sending
This version adds outgoing SMS using Android's `SEND_SMS` runtime permission. New conversations require a phone number in international format (for example, +491701234567). SMS uses the device SIM and carrier network; it does not use internet, but carrier charges may apply. Android's call to SmsManager does not prove recipient delivery. Incoming SMS reception is not included in this build.
