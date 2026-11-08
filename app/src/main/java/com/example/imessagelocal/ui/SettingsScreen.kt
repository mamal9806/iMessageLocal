package com.example.imessagelocal.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.imessagelocal.data.Settings

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.settings
    val context = LocalContext.current
    var simOptions by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
        var showAbout by remember { mutableStateOf(false) }
    var showSim by remember { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val phoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) showSim = true }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val soundPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val picked = if (Build.VERSION.SDK_INT >= 33) result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, android.net.Uri::class.java) else @Suppress("DEPRECATION") result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            vm.updateSettings(vm.settings.copy(notificationSoundUri = picked?.toString()))
        }
    }

    Column(Modifier.fillMaxSize().padding(bottom = 92.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 20.dp, top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت", tint = TextPrimary) }
            Text("تنظیمات", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            SettingsHero()
            Spacer(Modifier.height(18.dp))
            SectionTitle("عمومی")
            SettingsCard {
                SettingRow(Icons.Default.Language, "زبان برنامه", "فارسی", null)
                DividerLine()
                SettingRow(Icons.Default.VolumeUp, "صدای نوتیفیکیشن", if (s.sound) "صدای پیش‌فرض" else "خاموش") { vm.updateSettings(s.copy(sound = !s.sound)) }
                DividerLine()
                SettingRow(Icons.Default.Phone, "سیم‌کارت پیش‌فرض", if (s.defaultSimSubscriptionId < 0) "پیش‌فرض سیستم" else "SIM انتخاب‌شده") {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) { simOptions = activeSims(context); showSim = true } else phoneLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                }
            }
            Spacer(Modifier.height(14.dp))
            SectionTitle("حریم خصوصی")
            SettingsCard {
                SettingRow(Icons.Default.Lock, "دسترسی‌ها و مجوزها", "مجوزهای موردنیاز پیامک") {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                    else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                DividerLine()
                SettingRow(Icons.Default.Notifications, "اعلان‌ها", if (s.sound) "فعال" else "بی‌صدا") { if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
            Spacer(Modifier.height(14.dp))
            SectionTitle("اطلاعات بیشتر")
            SettingsCard {
                SettingRow(Icons.Default.Info, "درباره برنامه", "A SMS · نسخه 2.0.0") { showAbout = true }
                DividerLine()
                SettingRow(Icons.Default.Public, "برنامه پیش‌فرض پیامک", "برای دریافت پیامک واقعی") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val role = context.getSystemService(RoleManager::class.java)
                        if (role.isRoleAvailable(RoleManager.ROLE_SMS) && !role.isRoleHeld(RoleManager.ROLE_SMS)) roleLauncher.launch(role.createRequestRoleIntent(RoleManager.ROLE_SMS))
                    } else roleLauncher.launch(Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("A SMS\nFast · Beautiful · Secure", color = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showSim) {
        AlertLikeDialog("انتخاب سیم‌کارت", buildList {
            add("پیش‌فرض سیستم" to false)
            simOptions.forEachIndexed { i, info -> add("SIM ${i + 1} · ${info.carrierName ?: "اپراتور"}" to (s.defaultSimSubscriptionId == info.subscriptionId)) }
        }) { selected ->
            if (selected == "پیش‌فرض سیستم") vm.updateSettings(s.copy(defaultSimSubscriptionId = -1))
            else simOptions.firstOrNull { "SIM ${simOptions.indexOf(it) + 1} · ${it.carrierName ?: "اپراتور"}" == selected }?.let { vm.updateSettings(s.copy(defaultSimSubscriptionId = it.subscriptionId)) }
            showSim = false
        }
    }
    if (showAbout) {
        AlertLikeDialog("درباره A SMS", listOf("نسخه 2.0.0" to false, "پیام‌رسان آفلاین · بدون حساب و سرور" to false)) { showAbout = false }
    }
}

@Composable
private fun SettingsHero() {
    Row(Modifier.fillMaxWidth().liquidGlass().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(BrushIcon)) { Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.padding(15.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("A SMS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("ساخته شده با ❤️ برای شما", color = TextSecondary)
        }
    }
}

@Composable
private fun SectionTitle(text: String) { Text(text, color = TextSecondary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)) }

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xAA101A31)).border(1.dp, Color.White.copy(alpha = .06f), RoundedCornerShape(22.dp))) { content() }
}

@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)?) {
    val rowModifier = if (onClick != null) Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 15.dp) else Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 15.dp)
    Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = .07f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = IOSBlue, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(value, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) Text("›", color = TextSecondary, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable private fun DividerLine() { Spacer(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f))) }

@Composable
private fun AlertLikeDialog(title: String, options: List<Pair<String, Boolean>>, onSelected: (String) -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onSelected("") },
        title = { Text(title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { options.forEach { (label, selected) -> TextButton(onClick = { onSelected(label) }, modifier = Modifier.fillMaxWidth()) { Text(if (selected) "✓  $label" else label) } } } },
        confirmButton = { TextButton(onClick = { onSelected("") }) { Text("لغو") } },
    )
}

private val BrushIcon = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF1D9BFF), Color(0xFF3C5CFF), Color(0xFF8D35FF)))

