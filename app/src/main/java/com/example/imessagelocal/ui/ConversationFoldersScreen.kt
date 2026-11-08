package com.example.imessagelocal.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.ConversationEntity

@Composable
fun ConversationFoldersScreen(vm: AppViewModel, folder: String, onBack: () -> Unit, onOpen: (Long) -> Unit) {
    val english = vm.settings.language == 1
    val all by vm.allConversations.collectAsStateWithLifecycle()
    val rows = all.filter {
        when (folder) {
            "archive" -> it.archived
            "trash" -> it.trashed
            "spam" -> it.spam
            "blocked" -> it.blocked
            else -> false
        }
    }
    var selected by remember { mutableStateOf<ConversationEntity?>(null) }
    val title = when (folder) {
        "archive" -> if (english) "Archive" else "آرشیو"
        "trash" -> if (english) "Trash" else "سطل زباله"
        "spam" -> if (english) "Spam" else "اسپم"
        else -> if (english) "Blocked" else "مسدودشده‌ها"
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text(if (english) "Back" else "بازگشت") }
        }
        if (rows.isEmpty()) {
            Text(if (english) "This folder is empty." else "این پوشه خالی است.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(rows, key = { it.id }) { c ->
                    FolderConversationRow(c, english, onClick = { onOpen(c.id) }, onLongClick = { selected = c })
                }
            }
        }
    }
    selected?.let { c ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(c.name) },
            text = { Text(if (english) "Choose an action" else "یک عملیات انتخاب کنید") },
            confirmButton = {
                TextButton(onClick = {
                    vm.moveConversationToFolder(c.id, "normal")
                    selected = null
                }) { Text(if (english) "Restore to inbox" else "بازگردانی به پیام‌ها") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (folder == "trash") vm.permanentlyDeleteConversation(c.id)
                    else vm.moveConversationToFolder(c.id, "trash")
                    selected = null
                }) { Text(if (folder == "trash") { if (english) "Delete permanently" else "حذف دائمی" } else { if (english) "Move to trash" else "انتقال به سطل زباله" }, color = MaterialTheme.colorScheme.error) }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderConversationRow(c: ConversationEntity, english: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(vertical = 13.dp),
    ) {
        Text(c.name, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Text(c.lastText.ifBlank { c.phoneNumber }, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
