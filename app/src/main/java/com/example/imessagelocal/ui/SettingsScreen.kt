package com.example.imessagelocal.ui

import androidx.compose.foundation.layout.Arrangement
import android.app.role.RoleManager
import android.os.Build
import android.provider.Telephony
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.settings
    val context = LocalContext.current
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Messaging", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(RoleManager::class.java)
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                        roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
                    }
                } else {
                    roleLauncher.launch(android.content.Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                        .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName))
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Set as default SMS app") }
            Text("Incoming SMS reception and SMS-provider integration require this app to hold the system SMS role.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (Build.VERSION.SDK_INT >= 33) {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Allow notifications") }
            }

            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("System" to 0, "Light" to 1, "Dark" to 2).forEach { (label, value) ->
                    FilterChip(
                        selected = s.darkMode == value,
                        onClick = { vm.updateSettings(s.copy(darkMode = value)) },
                        label = { Text(label) },
                    )
                }
            }

            Text("Text size: ${"%.2f".format(s.fontScale)}x")
            Slider(
                value = s.fontScale,
                onValueChange = { vm.updateSettings(s.copy(fontScale = it)) },
                valueRange = 0.85f..1.3f,
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Notification sound", Modifier.weight(1f))
                Switch(
                    checked = s.sound,
                    onCheckedChange = { vm.updateSettings(s.copy(sound = it)) },
                )
            }

            HorizontalDivider()

            Text("Privacy", style = MaterialTheme.typography.titleMedium)
            Text(
                "Everything stays in this phone's local database. The app asks for no internet " +
                    "permission and has no accounts, ads, or servers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
