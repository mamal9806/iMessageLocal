# Build / QA report — A SMS redesign

## Completed

- Inspected the supplied Android project structure, Manifest, Room database, SMS receivers, notification code and existing Compose screens.
- Reworked the Compose UI into a Persian RTL, dark, glass/gradient design system following the supplied reference: message list, chat, contacts and settings.
- Preserved the existing Room database name and migration path.
- Preserved real SMS send/receive, multipart SMS, SIM subscription selection, delivery callbacks, notifications, contact matching and default-SMS role handling.
- Added a real device-contacts screen backed by `ContactsContract`.
- Updated application branding to `A SMS`.
- Added Material icon support and a dark visual theme.
- Added README documentation with the real testing limitations.

## Build verification

A local Gradle/Android SDK toolchain is not installed in the execution environment. The project therefore was **not** falsely reported as compiled here. Final APK compilation must be performed in Android Studio or CI with Android SDK 35 and the configured Gradle/AGP versions.

## Device QA still required

1. Make A SMS the Android Default SMS app.
2. Grant SMS, phone/SIM, contacts and notification permissions as requested.
3. Verify single-part and multipart outgoing SMS.
4. Verify incoming SMS insertion and notification.
5. Verify conversations are matched by phone number.
6. Verify SIM selection on dual-SIM hardware.
7. Verify carrier delivery reports when supported.
8. Verify Android 10+ SMS role behavior and SENDTO/RESPOND_VIA_MESSAGE flows.
