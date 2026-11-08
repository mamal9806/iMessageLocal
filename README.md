# A SMS — redesigned offline Android SMS client

A SMS is an offline-first Android SMS application built with Kotlin, Jetpack Compose, Material 3, MVVM and Room. This revision keeps the existing local database and SMS transport layer while replacing the UI with a dark, Persian RTL, glass/gradient visual system based on the supplied reference image.

## Main screens

- پیام‌ها: conversation list, search, pinned conversations, contact picker and new conversation.
- گفت‌وگو: real SMS sending, multipart SMS, delivery state, reply/edit/delete actions and per-conversation SIM selection.
- مخاطبین: reads the device phone contacts after permission is granted and opens/creates real SMS conversations.
- تنظیمات: notification sound, notification permission, contact permissions, default SIM, default SMS role and app information.

## SMS behavior retained

- Real carrier SMS through `SmsManager`.
- Multipart SMS.
- Incoming `SMS_DELIVER` handling.
- Room persistence and conversation matching by phone number.
- Contact-name lookup for incoming senders when `READ_CONTACTS` is granted.
- Default SMS role request flow.
- Per-conversation and default SIM subscription selection.
- Sent-to-network / delivered / failed states where Android and the carrier provide them.
- Notification channels and conversation deep links.
- No INTERNET permission, account, server, online chat, advertising or cloud dependency.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35. The project uses Android Gradle Plugin 8.5.2 and Kotlin 2.0.20.

Run:

```text
:app:assembleDebug
```

A Gradle wrapper is not bundled in this source archive. In an Android Studio installation, use the IDE's configured Gradle distribution or generate the wrapper with the project's configured Gradle version.

## Testing status

The source was statically reviewed and the SMS/data paths from the supplied project were retained. A local Android APK build could not be executed in this environment because Gradle and the Android SDK are not installed here. Therefore this delivery does **not** claim a locally successful APK build or real-device SMS test.

For real-device validation, install as the default SMS application, grant SMS/contacts/notification permissions, verify SIM selection, send a multipart message, receive an SMS, and confirm the system SMS provider/notification behavior.

## Platform limitation

`DELIVERED` is only shown when the carrier returns a delivery report. `SENT_TO_NETWORK` means Android/carrier accepted the send request; it does not prove that the recipient read or received the message.
