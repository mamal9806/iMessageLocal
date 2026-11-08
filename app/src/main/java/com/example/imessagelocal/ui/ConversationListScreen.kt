package com.example.imessagelocal.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.ConversationEntity

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onSettings: () -> Unit) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    val messageCount by vm.totalMessageCount.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var menuFor by remember { mutableStateOf<ConversationEntity?>(null) }
    var incomingFor by remember { mutableStateOf<ConversationEntity?>(null) }
    var incomingText by remember { mutableStateOf("") }
    var deleteFor by remember { mutableStateOf<ConversationEntity?>(null) }
    val context = LocalContext.current
    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    newName = cursor.getString(0).orEmpty()
                    newPhone = cursor.getString(1).orEmpty()
                    showNew = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("A SMS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "$messageCount پیام · ${conversations.size} گفتگو",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier.size(42.dp).liquidGlassCircle(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        onClick = onSettings,
                    ) { Text("⚙", color = IOSBlue, style = MaterialTheme.typography.titleLarge) }
                    TextButton(
                        modifier = Modifier.size(42.dp).liquidGlassCircle(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        onClick = {
                            contactPicker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
                        },
                    ) { Text("♙", color = IOSBlue, style = MaterialTheme.typography.titleLarge) }
                    TextButton(
                        modifier = Modifier.size(42.dp).liquidGlassCircle(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        onClick = { newName = ""; newPhone = ""; showNew = true },
                    ) { Text("+", color = IOSBlue, style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.setQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                placeholder = { Text("⌕  جستجوی نام، شماره یا پیام") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )

            if (conversations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isBlank()) "هنوز گفتگویی نداری. برای شروع روی + بزن."
                        else "نتیجه‌ای برای جستجو پیدا نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(conversations, key = { it.id }) { c ->
                        Box {
                            Row(
                                Modifier.fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onOpen(c.id) },
                                        onLongClick = { menuFor = c },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(54.dp).clip(CircleShape).background(Color(c.avatarColor)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        c.name.take(1).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            (if (c.pinned) "📌 " else "") + c.name,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            formatListTime(c.updatedAt),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        c.lastText.ifBlank { "بدون پیام" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            DropdownMenu(expanded = menuFor?.id == c.id, onDismissRequest = { menuFor = null }) {
                                DropdownMenuItem(
                                    text = { Text(if (c.pinned) "📌 برداشتن سنجاق" else "📌 سنجاق گفتگو") },
                                    onClick = { vm.togglePin(c); menuFor = null },
                                )
                                DropdownMenuItem(
                                    text = { Text("✉ ثبت پیام دریافتی") },
                                    onClick = { incomingFor = c; incomingText = ""; menuFor = null },
                                )
                                DropdownMenuItem(
                                    text = { Text("⌫ حذف گفتگو", color = MaterialTheme.colorScheme.error) },
                                    onClick = { deleteFor = c; menuFor = null },
                                )
                            }
                        }
                        HorizontalDivider(
                            Modifier.padding(start = 80.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        )
                    }
                }
            }
        }
    }

    if (showNew) {
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("گفتگوی جدید") },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, singleLine = true, placeholder = { Text("نام مخاطب") })
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, singleLine = true, placeholder = { Text("شماره تلفن") })
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank() && newPhone.isNotBlank(),
                    onClick = { vm.createConversation(newName, newPhone); showNew = false },
                ) { Text("ایجاد گفتگو") }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { showNew = false }) { Text("لغو") } },
        )
    }

    incomingFor?.let { c ->
        AlertDialog(
            onDismissRequest = { incomingFor = null },
            title = { Text("پیام از ${c.name}") },
            text = {
                OutlinedTextField(value = incomingText, onValueChange = { incomingText = it }, placeholder = { Text("متن پیام") })
            },
            confirmButton = {
                TextButton(
                    enabled = incomingText.isNotBlank(),
                    onClick = { vm.logIncoming(c.id, incomingText); incomingFor = null },
                ) { Text("ذخیره") }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { incomingFor = null }) { Text("لغو") } },
        )
    }

    deleteFor?.let { c ->
        AlertDialog(
            onDismissRequest = { deleteFor = null },
            title = { Text("حذف ${c.name}؟") },
            text = { Text("تمام پیام‌های این گفتگو از پایگاه داده محلی گوشی حذف می‌شوند.") },
            confirmButton = {
                TextButton(
                    modifier = Modifier.liquidGlass(),
                    onClick = { vm.deleteConversation(c.id); deleteFor = null },
                ) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { deleteFor = null }) { Text("لغو") } },
        )
    }
}
