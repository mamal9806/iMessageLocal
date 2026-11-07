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
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(modifier = Modifier.liquidGlass(), onClick = onSettings) { Text("Settings") } },
                actions = {
                    TextButton(modifier = Modifier.liquidGlass(), onClick = { contactPicker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)) }) { Text("Contacts") }
                    TextButton(modifier = Modifier.liquidGlass(), onClick = { newName = ""; newPhone = ""; showNew = true }) { Text("New") }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                placeholder = { Text("Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            if (conversations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isBlank()) "No conversations yet. Tap New to start one."
                        else "Nothing matches that search.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(conversations, key = { it.id }) { c ->
                        Box {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onOpen(c.id) },
                                        onLongClick = { menuFor = c },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(52.dp).clip(CircleShape).background(Color(c.avatarColor)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        c.name.take(1),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            if (c.pinned) "📌 ${c.name}" else c.name,
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
                                        c.lastText.ifBlank { "No messages yet" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = menuFor?.id == c.id,
                                onDismissRequest = { menuFor = null },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (c.pinned) "Unpin" else "Pin") },
                                    onClick = {
                                        vm.togglePin(c)
                                        menuFor = null
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Log incoming message") },
                                    onClick = {
                                        incomingFor = c
                                        incomingText = ""
                                        menuFor = null
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        deleteFor = c
                                        menuFor = null
                                    },
                                )
                            }
                        }
                        HorizontalDivider(
                            Modifier.padding(start = 80.dp),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }

    if (showNew) {
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("New conversation") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        placeholder = { Text("Name") },
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        singleLine = true,
                        placeholder = { Text("Phone number, e.g. +4917...") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank() && newPhone.isNotBlank(),
                    onClick = {
                        vm.createConversation(newName, newPhone)
                        showNew = false
                    },
                ) { Text("Create") }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { showNew = false }) { Text("Cancel") } },
        )
    }

    incomingFor?.let { c ->
        AlertDialog(
            onDismissRequest = { incomingFor = null },
            title = { Text("Message from ${c.name}") },
            text = {
                OutlinedTextField(
                    value = incomingText,
                    onValueChange = { incomingText = it },
                    placeholder = { Text("What did they say?") },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = incomingText.isNotBlank(),
                    onClick = {
                        vm.logIncoming(c.id, incomingText)
                        incomingFor = null
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { incomingFor = null }) { Text("Cancel") } },
        )
    }

    deleteFor?.let { c ->
        AlertDialog(
            onDismissRequest = { deleteFor = null },
            title = { Text("Delete ${c.name}?") },
            text = { Text("All messages in this conversation will be removed from this phone.") },
            confirmButton = {
                TextButton(modifier = Modifier.liquidGlass(), onClick = {
                    vm.deleteConversation(c.id)
                    deleteFor = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { deleteFor = null }) { Text("Cancel") } },
        )
    }
}
