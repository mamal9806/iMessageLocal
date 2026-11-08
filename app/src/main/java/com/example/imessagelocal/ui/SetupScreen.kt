package com.example.imessagelocal.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.imessagelocal.data.Settings

@Composable
fun SetupScreen(vm: AppViewModel, onComplete: () -> Unit) {
    val context = LocalContext.current
    var roleGranted by remember { mutableStateOf(Telephony.Sms.getDefaultSmsPackage(context) == context.packageName) }
    var permissionSummary by remember { mutableStateOf("برای استفاده کامل، مجوزهای لازم را فعال کنید.") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.count { it.value }
        permissionSummary = "$granted مجوز از ${result.size} مجوز درخواست‌شده فعال شد."
    }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        roleGranted = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        permissionSummary = if (roleGranted) "A SMS برنامه پیش‌فرض پیامک شد." else "انتخاب برنامه پیش‌فرض انجام نشد."
    }
    val restorePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.restorePreviousMessages() else permissionSummary = "برای بازیابی، مجوز خواندن پیامک لازم است."
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Sms, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(64.dp))
        Spacer(Modifier.height(14.dp))
        Text("به A SMS خوش آمدید", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("چند مرحله کوتاه تا آماده شدن برنامه", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("دسترسی‌های موردنیاز", fontWeight = FontWeight.SemiBold)
                        Text("ارسال و دریافت پیامک، مخاطبین، وضعیت سیم‌کارت و اعلان‌ها", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Button(onClick = {
                    val permissions = buildList {
                        add(Manifest.permission.SEND_SMS)
                        add(Manifest.permission.RECEIVE_SMS)
                        add(Manifest.permission.READ_SMS)
                        add(Manifest.permission.READ_CONTACTS)
                        add(Manifest.permission.READ_PHONE_STATE)
                        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                    }.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                    if (permissions.isNotEmpty()) permissionLauncher.launch(permissions.toTypedArray())
                    else permissionSummary = "تمام مجوزهای درخواستی قبلاً فعال شده‌اند."
                }, modifier = Modifier.fillMaxWidth()) { Text("فعال‌سازی دسترسی‌ها") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (roleGranted) Icons.Default.CheckCircle else Icons.Default.Forum, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("برنامه پیش‌فرض پیامک", fontWeight = FontWeight.SemiBold)
                        Text(if (roleGranted) "A SMS برنامه پیش‌فرض است" else "برای دریافت پیامک و بازیابی تاریخچه لازم است", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val manager = context.getSystemService(RoleManager::class.java)
                        if (manager.isRoleAvailable(RoleManager.ROLE_SMS) && !manager.isRoleHeld(RoleManager.ROLE_SMS)) roleLauncher.launch(manager.createRequestRoleIntent(RoleManager.ROLE_SMS))
                        else roleGranted = manager.isRoleHeld(RoleManager.ROLE_SMS)
                    } else {
                        roleLauncher.launch(Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text(if (roleGranted) "برنامه پیش‌فرض تنظیم شده" else "انتخاب A SMS به‌عنوان برنامه پیامک") }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("بازیابی پیام‌های قبلی", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("پیامک‌های دریافتی و ارسالی از برنامه قبلی، با تاریخ اصلی به گفتگوها منتقل می‌شوند. پیام‌های تکراری دوباره وارد نمی‌شوند.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(enabled = roleGranted, onClick = { restorePermissionLauncher.launch(Manifest.permission.READ_SMS) }, modifier = Modifier.fillMaxWidth()) {
                    Text("بازیابی پیامک‌های قبلی")
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(permissionSummary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        vm.smsStatus.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("پایان راه‌اندازی و ورود به برنامه")
        }
        Text("هر زمان لازم باشد می‌توانید مجوزها را از تنظیمات اندروید مدیریت کنید.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
    }
}
