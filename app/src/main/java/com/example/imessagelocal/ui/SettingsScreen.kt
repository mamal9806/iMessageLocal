package com.example.imessagelocal.ui

import android.Manifest
import android.app.role.RoleManager
import android.media.RingtoneManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.settings
    val context = LocalContext.current
    var simOptions by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
    var simMessage by remember { mutableStateOf("") }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val historyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.importSmsHistory()
        else simMessage = "برای انتقال تاریخچه، مجوز خواندن پیامک لازم است."
    }
    val simPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            simOptions = try {
                context.getSystemService(SubscriptionManager::class.java).activeSubscriptionInfoList.orEmpty()
            } catch (_: SecurityException) { emptyList() }
            simMessage = if (simOptions.isEmpty()) "سیم‌کارت فعالی پیدا نشد." else ""
        } else simMessage = "برای نمایش سیم‌کارت‌ها مجوز تلفن لازم است."
    }
    val soundPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val picked = if (Build.VERSION.SDK_INT >= 33) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, android.net.Uri::class.java)
            } else {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            vm.updateSettings(vm.settings.copy(notificationSoundUri = picked?.toString()))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    TextButton(modifier = Modifier.liquidGlass(), onClick = onBack) {
                        Text("‹  بازگشت")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("✉  پیام‌رسانی", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = context.getSystemService(RoleManager::class.java)
                        if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                            roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
                        }
                    } else {
                        roleLauncher.launch(
                            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().liquidGlass(),
            ) { Text("✓  انتخاب به‌عنوان برنامه پیامک پیش‌فرض") }
            Text(
                "برای دریافت پیامک و مدیریت صندوق پیامک گوشی، باید A SMS برنامه پیش‌فرض پیامک باشد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                        vm.importSmsHistory()
                    } else historyLauncher.launch(Manifest.permission.READ_SMS)
                },
                modifier = Modifier.fillMaxWidth().liquidGlass(),
            ) { Text("↻  انتقال تاریخچه پیامک‌های گوشی") }
            Text(
                "پیام‌های دریافتی و ارسالی از صندوق پیامک گوشی بررسی می‌شوند؛ موارد تکراری دوباره ثبت نمی‌شوند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (vm.smsStatus.isNotBlank()) {
                Text(vm.smsStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                },
                modifier = Modifier.fillMaxWidth().liquidGlass(),
            ) { Text("♙  اجازه نمایش نام مخاطبان") }

            if (Build.VERSION.SDK_INT >= 33) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().liquidGlass(),
                ) { Text("♢  فعال‌سازی اعلان‌ها") }
            }

            HorizontalDivider()
            Text("▣  سیم‌کارت پیش‌فرض", style = MaterialTheme.typography.titleMedium)
            Text(
                "برای گفتگوهایی استفاده می‌شود که سیم‌کارت اختصاصی ندارند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.defaultSimSubscriptionId == -1,
                    onClick = { vm.updateSettings(s.copy(defaultSimSubscriptionId = -1)) },
                    label = { Text("پیش‌فرض سیستم") },
                )
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            simOptions = try {
                                context.getSystemService(SubscriptionManager::class.java).activeSubscriptionInfoList.orEmpty()
                            } catch (_: SecurityException) { emptyList() }
                            simMessage = if (simOptions.isEmpty()) "سیم‌کارت فعالی پیدا نشد." else ""
                        } else simPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                    },
                    modifier = Modifier.liquidGlass(),
                ) { Text("انتخاب سیم‌کارت") }
            }
            simOptions.forEachIndexed { index, info ->
                FilterChip(
                    selected = s.defaultSimSubscriptionId == info.subscriptionId,
                    onClick = { vm.updateSettings(s.copy(defaultSimSubscriptionId = info.subscriptionId)) },
                    label = { Text("SIM ${index + 1} · ${info.carrierName}") },
                )
            }
            if (simMessage.isNotBlank()) Text(simMessage, color = MaterialTheme.colorScheme.error)

            HorizontalDivider()
            Text("◐  ظاهر برنامه", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("سیستم" to 0, "روشن" to 1, "تاریک" to 2).forEach { (label, value) ->
                    FilterChip(
                        selected = s.darkMode == value,
                        onClick = { vm.updateSettings(s.copy(darkMode = value)) },
                        label = { Text(label) },
                    )
                }
            }
            Text("اندازه نوشته: ${"%.2f".format(s.fontScale)}×")
            Slider(
                value = s.fontScale,
                onValueChange = { vm.updateSettings(s.copy(fontScale = it)) },
                valueRange = 0.85f..1.3f,
            )

            HorizontalDivider()
            Text("♫  اعلان‌ها", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("صدای اعلان", Modifier.weight(1f))
                Switch(checked = s.sound, onCheckedChange = { vm.updateSettings(s.copy(sound = it)) })
            }
            Button(
                onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose notification sound")
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        s.notificationSoundUri?.let {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, android.net.Uri.parse(it))
                        }
                    }
                    soundPicker.launch(intent)
                },
                modifier = Modifier.fillMaxWidth().liquidGlass(),
            ) { Text("♫  انتخاب صدای اعلان") }
            Text(
                s.notificationSoundUri?.let { "صدای اختصاصی انتخاب شده است" } ?: "از صدای پیش‌فرض گوشی استفاده می‌شود",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()
            Text("ⓘ  درباره A SMS", style = MaterialTheme.typography.titleMedium)
            Text("A SMS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("پیام‌رسان پیامک محلی برای اندروید", style = MaterialTheme.typography.bodyMedium)
            Text("سازنده: mamaD(kazeroon)", style = MaterialTheme.typography.bodyMedium)
            Text("ایمیل: pishi1403@gmail.com", style = MaterialTheme.typography.bodyMedium)
            Text(
                "پیام‌ها روی همین گوشی نگهداری می‌شوند. برای کارکرد پایه برنامه نیازی به حساب کاربری یا سرور نیست.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
