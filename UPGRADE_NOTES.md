# iMessage Local – UI and messaging upgrades

- Added a global default SIM selector in Settings. Conversation-specific SIM selection still takes priority; otherwise the app uses this setting, then Android's system default.
- Added a notification ringtone picker. A new notification channel is created for each selected tone because Android locks channel sound after channel creation. If Android has already created a channel, use the app notification settings to manage it.
- Added reflective glass-style highlights and fine borders to primary controls and message bubbles.
- Replaced raw delivery-state text in outgoing bubbles with compact status marks: clock while pending, one check when accepted by the network, double blue checks when the carrier reports delivery, and an alert mark on failure.
- Added iOS-like back and save/check glyphs.
- Emoji can be entered through the phone keyboard. Image stickers are not transmitted as ordinary SMS; sending image stickers requires MMS support and an MMS send implementation, which this app does not currently include.
