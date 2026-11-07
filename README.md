# iMessage Local

An offline-first Android SMS client built with Kotlin, Jetpack Compose, Material 3, MVVM and Room. The project keeps the existing `imessage_local.db` database name and migrates the existing Room v1/v2 data forward without destructive fallback.

## Build

- Android Studio with JDK 17 and Android SDK 35.
- Gradle 8.7 / Android Gradle Plugin 8.5.2.
- Open this directory and run `:app:assembleDebug`.
- GitHub Actions builds Debug and Release APKs on push, pull request, and manual dispatch, then uploads both as artifacts.

## Implemented in this update

- Room schema v3 with non-destructive migrations 1→2 and 2→3.
- Contact picker integration using Android's system phone-number picker. The selected phone-number row determines which number is used; the app does not maintain a separate address book.
- Real carrier SMS sending through `SmsManager`, including multipart messages, per-conversation SIM subscription selection, and sent-result callbacks persisted as message status.
- Incoming `SMS_DELIVER` receiver with multipart body assembly, duplicate guard, Room persistence, SMS inbox provider insertion, and notifications.
- Android SMS-role request flow (RoleManager on Android 10+, legacy default-SMS change intent on Android 8/9), SENDTO intent handling, RESPOND_VIA_MESSAGE service, and WAP_PUSH_DELIVER registration.
- Notification channels, conversation deep links, adaptive launcher icon, and GitHub Actions workflow.

## Important platform and product limitations

- `SENT_TO_NETWORK` means Android/the carrier accepted the send request; it is **not** proof that the recipient received or read the SMS. Carrier delivery reports are not enabled by this implementation.
- Incoming SMS delivery and writing the system SMS inbox require the user to grant this app the system Default SMS role. Android/Google Play restrict SMS permissions and role eligibility.
- WAP Push is registered for SMS-role eligibility, but full MMS/PDU/media transport is not implemented. MMS is not advertised as supported.
- Contact selection uses Android's system picker and reads the selected phone row. Contact photo retrieval, multi-number chooser UI beyond the system picker, and contact URI/avatar synchronization are not yet implemented.
- Conversation-level notification preferences, archive/backup/restore, attachments, biometric app lock, multi-message selection, and full settings coverage are not yet implemented.
- Per-conversation SIM selection is saved and passed to `SmsManager`; carrier/device firmware may still reject a subscription that becomes inactive. If no SIM is selected, Android's system default is used.
- No Internet permission or server dependency is added.

## Testing

The project source and Android manifest have been reviewed and XML files are checked for well-formedness. A local APK build could not be run in this execution environment because Android SDK/Gradle tooling is not installed. The GitHub Actions workflow is configured to perform actual Debug and Release builds in a hosted Android environment.
