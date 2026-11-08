# A SMS — UI and messaging upgrades

- Rebuilt the Compose UI around a dark Persian RTL visual system matching the supplied reference: blue/purple glow, glass cards, rounded panels, bottom navigation and chat bubbles.
- Added a real device Contacts destination using `ContactsContract` and the existing conversation repository.
- Kept global default SIM selection and per-conversation SIM selection.
- Kept real carrier SMS sending, multipart messages, incoming SMS delivery, notification channels and delivery callbacks.
- Updated app branding to A SMS.
- Kept notification ringtone support from the previous implementation.
- No Internet/server/account layer was introduced.
- MMS remains intentionally unsupported beyond WAP-push role registration.
