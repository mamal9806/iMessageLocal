# A SMS — Android SMS Messenger

**فارسی | [English](#english)**

A SMS یک پیام‌رسان پیامکی محلی برای اندروید است که با Kotlin و Jetpack Compose ساخته شده و رابطی الهام‌گرفته از iOS، با پشتیبانی از پیامک واقعی و ذخیره‌سازی محلی ارائه می‌دهد.

## فارسی

### ✨ امکانات فعلی

- ارسال و دریافت پیامک (SMS) با استفاده از سرویس پیامک اندروید
- نمایش فهرست گفتگوها، آخرین پیام و زمان آن
- جست‌وجوی گفتگوها و مخاطبین
- صفحه مخاطبین با خواندن مخاطبین ذخیره‌شده در گوشی
- نشان تعداد پیام‌های خوانده‌نشده کنار هر گفتگو؛ با بازکردن گفتگو شمارنده پاک می‌شود
- نمایش وضعیت ارسال و گزارش تحویل پیامک، در صورت پشتیبانی اپراتور
- انتخاب سیم‌کارت برای هر گفتگو و تعیین سیم‌کارت پیش‌فرض در تنظیمات
- پاسخ سریع از داخل اعلان پیامک ورودی (با قابلیت RemoteInput اندروید)
- پاسخ به پیام، ویرایش و حذف پیام
- سنجاق‌کردن و حذف گفتگوها
- تم روشن، تاریک یا پیروی از تنظیمات سیستم
- تنظیم صدای اعلان پیامک
- رابط فارسی راست‌چین با طراحی شیشه‌ای/گرادیانی
- صفحه «درباره برنامه» و اطلاعات سازنده

### 📱 پیش‌نیازها

- Android 8.0 (API 26) یا بالاتر
- برای ساخت پروژه: JDK 17 و Android SDK Platform 35
- برای ارسال و دریافت پیامک: گوشی دارای قابلیت SMS و سیم‌کارت فعال

### 🔐 مجوزها و برنامه پیامک پیش‌فرض

برای دریافت کامل پیامک‌های ورودی و مدیریت اعلان‌ها، A SMS باید با تأیید کاربر به‌عنوان **برنامه پیش‌فرض SMS** اندروید انتخاب شود. بسته به قابلیت‌هایی که استفاده می‌کنید، مجوزهای SMS، مخاطبین، وضعیت تلفن/سیم‌کارت و اعلان‌ها لازم هستند. اندروید درخواست مجوزها را هنگام نیاز نمایش می‌دهد.

### 🛠 ساخت پروژه

1. پروژه را در Android Studio باز کنید.
2. JDK 17 و Android SDK Platform 35 را نصب کنید.
3. Gradle Sync را اجرا کنید.
4. از مسیر **Build → Build Bundle(s) / APK(s) → Build APK(s)** فایل APK را بسازید.

> در نسخه فعلی مخزن، Gradle Wrapper داخل پروژه قرار ندارد؛ بنابراین ساخت از خط فرمان نیازمند نصب و پیکربندی Gradle است.

### ⚠️ وضعیت و محدودیت‌ها

- این پروژه برای پیامک (SMS) است؛ iMessage اپل یا پیام‌رسان اینترنتی نیست.
- ارسال پیامک، هزینه و محدودیت‌های اپراتور را دارد.
- گزارش تحویل فقط در صورت پشتیبانی شبکه و اپراتور قابل اتکاست و تضمین نمی‌شود.
- پاسخ سریع اعلان به مجوز ارسال پیامک و سیم‌کارت/سرویس فعال نیاز دارد.
- پشتیبانی از MMS و ارسال استیکر تصویری در این نسخه به‌عنوان قابلیت تضمین‌شده معرفی نمی‌شود.
- فایل APK و تست نهایی روی دستگاه واقعی باید جداگانه انجام و تأیید شود؛ صرف وجود سورس به معنی تأییدشدن همه قابلیت‌ها روی تمام گوشی‌ها نیست.

### 👤 سازنده

- **نام سازنده:** mamaD (kazeroon)
- **ایمیل:** pishi1403@gmail.com

---

## English

A SMS is a local Android SMS messaging app built with **Kotlin** and **Jetpack Compose**. It combines real carrier-based SMS functionality with an iOS-inspired interface and on-device conversation storage.

### ✨ Current features

- Send and receive carrier-based SMS messages
- Conversation list with latest-message previews and timestamps
- Search conversations and contacts
- Browse contacts stored on the device
- Per-conversation unread-message counters; opening a conversation clears its unread count
- SMS sending status and carrier delivery reports, when supported by the network
- Choose a SIM for an individual conversation and configure the default SIM
- Quick reply directly from incoming-message notifications using Android RemoteInput
- Reply to, edit, and delete messages
- Pin and delete conversations
- Light theme, dark theme, or follow system appearance
- Configure the SMS notification sound
- Persian RTL interface with glass/gradient-inspired styling
- About screen with creator information

### 📱 Requirements

- Android 8.0 (API 26) or later
- JDK 17 and Android SDK Platform 35 to build the project
- An SMS-capable Android device with an active SIM for carrier messaging

### 🔐 Permissions and default SMS app

To receive incoming SMS reliably and manage the SMS experience, the user must approve A SMS as the Android **default SMS app**. Depending on the features used, SMS, contacts, phone/SIM-state, and notification permissions may be requested by Android.

### 🛠 Build

1. Open the project in Android Studio.
2. Install JDK 17 and Android SDK Platform 35.
3. Sync the project with Gradle.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

> The current repository does not include the Gradle Wrapper. Command-line builds therefore require Gradle to be installed and configured separately.

### ⚠️ Notes and limitations

- A SMS is an SMS app, not Apple's iMessage and not an internet-based messenger.
- SMS availability and charges depend on the carrier and subscription.
- Delivery reports depend on carrier/network support and are not guaranteed.
- Notification quick reply requires SMS permission and an active SMS service/SIM.
- MMS and image-sticker sending are not advertised as verified features in this version.
- The APK still needs a successful build and real-device testing. Source-code availability alone does not guarantee compatibility with every device.

### 👤 Creator

- **Creator:** mamaD (kazeroon)
- **Email:** pishi1403@gmail.com

---

**Repository:** https://github.com/mamal9806/iMessageLocal
