package com.example.imessagelocal.ui

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.MessageEntity

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: AppViewModel, conversationId: Long, onBack: () -> Unit) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val title = vm.conversationName(conversationId)
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current

    var draft by rememberSaveable { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<MessageEntity?>(null) }
    var menuFor by remember { mutableStateOf<MessageEntity?>(null) }
    var editing by remember { mutableStateOf<MessageEntity?>(null) }
    var editText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding().imePadding()) {
                replyTo?.let { r ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Replying to",
                                style = MaterialTheme.typography.labelMedium,
                                color = IOSBlue,
                            )
                            Text(
                                r.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TextButton(onClick = { replyTo = null }) { Text("Cancel") }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("iMessage") },
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 5,
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = draft.isNotBlank(),
                        onClick = {
                            vm.send(conversationId, draft.trim(), replyTo)
                            draft = ""
                            replyTo = null
                        },
                    ) { Text("Send") }
                }
            }
        },
    ) { padding ->
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(messages, key = { it.id }) { m ->
                    val mine = m.isOutgoing
                    val secondary = if (mine) Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (mine) IOSBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .combinedClickable(onClick = {}, onLongClick = { menuFor = m }),
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                m.replyPreview?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = secondary,
                                    )
                                }
                                Text(
                                    m.text,
                                    color = if (mine) Color.White else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    formatTime(m.createdAt) + if (m.edited) "  edited" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondary,
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = menuFor?.id == m.id,
                            onDismissRequest = { menuFor = null },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reply") },
                                onClick = {
                                    replyTo = m
                                    menuFor = null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = {
                                    clipboard.setText(AnnotatedString(m.text))
                                    menuFor = null
                                },
                            )
                            if (mine) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        editing = m
                                        editText = m.text
                                        menuFor = null
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    vm.deleteMessage(m)
                                    menuFor = null
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { m ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit message") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    maxLines = 5,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.editMessage(m, editText)
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
}
