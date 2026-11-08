package com.example.imessagelocal.ui

import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.ConversationEntity

@Composable
fun ConversationListScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onSettings: () -> Unit) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var menuFor by remember { mutableStateOf<ConversationEntity?>(null) }
    var confirmDeleteFor by remember { mutableStateOf<ConversationEntity?>(null) }
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

    Column(Modifier.fillMaxSize().padding(bottom = 92.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 14.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("پیام‌ها", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "تنظیمات", tint = TextSecondary) }
            IconButton(onClick = { contactPicker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)) }) {
                Icon(Icons.Default.Edit, "مخاطب جدید", tint = IOSBlue)
            }
        }

        SearchField(query) {
            query = it
            vm.setQuery(it)
        }

        if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (query.isBlank()) "هنوز گفتگویی وجود ندارد" else "نتیجه‌ای پیدا نشد", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    if (query.isBlank()) {
                        TextButton(onClick = { showNew = true }) { Text("شروع گفتگوی جدید", color = IOSBlue) }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                items(conversations, key = { it.id }) { c ->
                    ConversationRow(c, onClick = { onOpen(c.id) }, onLongClick = { menuFor = c })
                }
            }
        }
    }

    menuFor?.let { c ->
        AlertDialog(
            onDismissRequest = { menuFor = null },
            title = { Text(c.name) },
            text = { Text("عملیات گفتگو را انتخاب کنید") },
            confirmButton = {
                TextButton(onClick = { vm.togglePin(c); menuFor = null }) { Text(if (c.pinned) "برداشتن سنجاق" else "سنجاق کردن") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteFor = c; menuFor = null }) { Text("حذف گفتگو", color = MaterialTheme.colorScheme.error) }
            },
        )
    }

    confirmDeleteFor?.let { c ->
        AlertDialog(
            onDismissRequest = { confirmDeleteFor = null },
            title = { Text("حذف گفتگو؟") },
            text = { Text("تمام پیام‌های این گفتگو از A SMS حذف می‌شوند. این کار قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteConversation(c.id); confirmDeleteFor = null }) {
                    Text("حذف کامل", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteFor = null }) { Text("انصراف") } },
        )
    }

    if (showNew) {
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("گفتگوی جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(newName, { newName = it }, label = { Text("نام") }, singleLine = true)
                    OutlinedTextField(newPhone, { newPhone = it }, label = { Text("شماره تلفن") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = newName.isNotBlank() && newPhone.isNotBlank(), onClick = {
                    vm.createConversation(newName, newPhone); newName = ""; newPhone = ""; showNew = false
                }) { Text("ایجاد") }
            },
            dismissButton = { TextButton(onClick = { showNew = false }) { Text("لغو") } },
        )
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
        placeholder = { Text("جستجو", color = TextSecondary) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = .45f),
            focusedBorderColor = IOSBlue.copy(alpha = .7f),
        ),
    )
}

@Composable
private fun ConversationRow(c: ConversationEntity, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(c.avatarColor)), contentAlignment = Alignment.Center) {
            Text(c.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (c.pinned) "📌 ${c.name}" else c.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatListTime(c.updatedAt), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(3.dp))
            Text(c.lastText.ifBlank { c.phoneNumber }, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun BottomNav(selected: RootTab, onSelect: (RootTab) -> Unit) {
    // Kept separate so the visual shell can be shared by the three real destinations.
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp).clip(RoundedCornerShape(26.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = .96f)).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .35f)).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val items = listOf(
            Triple("پیام‌ها", RootTab.Messages, Icons.Default.Message),
            Triple("مخاطبین", RootTab.Contacts, Icons.Default.Contacts),
            Triple("تنظیمات", RootTab.Settings, Icons.Default.Settings),
        )
        items.forEach { (label, value, icon) ->
            val active = selected == value
            TextButton(onClick = { onSelect(value) }, modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = if (active) IOSBlue else TextSecondary, modifier = Modifier.size(20.dp))
                    Text(label, color = if (active) IOSBlue else TextSecondary, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

