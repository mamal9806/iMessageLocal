#!/data/data/com.termux/files/usr/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

python - <<'PY'
from pathlib import Path

def patch(path, replacements):
    p = Path(path)
    s = p.read_text(encoding="utf-8")
    original = s
    for old, new in replacements:
        if old in s:
            s = s.replace(old, new)
    if s != original:
        p.write_text(s, encoding="utf-8")
        print("Updated:", path)
    else:
        print("No text substitutions needed:", path)

patch("app/src/main/java/com/example/imessagelocal/ui/ChatScreen.kt", [
    ('Text("SMS", style = MaterialTheme.typography.labelSmall', 'Text("پیامک", style = MaterialTheme.typography.labelSmall'),
    ('Text("Replying to",', 'Text("پاسخ به",'),
    ('Text("Cancel")', 'Text("لغو")'),
    ('placeholder = { Text("Text Message · SMS") }', 'placeholder = { Text("پیام خود را بنویسید…") }'),
    ('Text("No messages yet",', 'Text("هنوز پیامی ردوبدل نشده",'),
    ('Text("Reply")', 'Text("پاسخ دادن")'),
    ('Text("Copy")', 'Text("کپی متن")'),
    ('Text("Edit")', 'Text("ویرایش")'),
    ('Text("Delete", color = MaterialTheme.colorScheme.error)', 'Text("حذف", color = MaterialTheme.colorScheme.error)'),
    ('Text("Choose SIM")', 'Text("انتخاب سیم‌کارت")'),
    ('Text("No active SIM was found. The system default SIM will be used.")', 'Text("سیم‌کارت فعالی پیدا نشد؛ از پیش‌فرض سیستم استفاده می‌شود.")'),
    ('Text("Use system default")', 'Text("استفاده از پیش‌فرض سیستم")'),
    ('Text("Close")', 'Text("بستن")'),
    ('Text("Edit message")', 'Text("ویرایش پیام")'),
    ('Text("Save")', 'Text("ذخیره")'),
])

patch("app/src/main/java/com/example/imessagelocal/ui/ConversationListScreen.kt", [
    ('Text("Search")', 'Text("جستجو")'),
    ('placeholder = { Text("Search") }', 'placeholder = { Text("جستجوی نام یا شماره") }'),
    ('Text("New conversation")', 'Text("گفتگوی جدید")'),
    ('Text("Name")', 'Text("نام مخاطب")'),
    ('Text("Phone number")', 'Text("شماره تلفن")'),
    ('Text("Create")', 'Text("ایجاد گفتگو")'),
    ('Text("Cancel")', 'Text("لغو")'),
    ('Text("Delete", color = MaterialTheme.colorScheme.error)', 'Text("حذف", color = MaterialTheme.colorScheme.error)'),
])
PY

echo
echo "A SMS dark Persian UI patch applied."
echo "Now inspect changes with: git status --short"
