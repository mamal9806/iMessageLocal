package com.example.imessagelocal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.SubscriptionInfo
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
    val context = LocalContext.current
    var draft by rememberSaveable { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<MessageEntity?>(null) }
    var menuFor by remember { mutableStateOf<MessageEntity?>(null) }
    var editing by remember { mutableStateOf<MessageEntity?>(null) }
    var editText by remember { mutableStateOf("") }
    var simOptions by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
    var showSimPicker by remember { mutableStateOf(false) }
    val simPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            simOptions = try { context.getSystemService(SubscriptionManager::class.java).activeSubscriptionInfoList.orEmpty() } catch (_: SecurityException) { emptyList() }
            showSimPicker = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && draft.isNotBlank()) {
            vm.sendSms(conversationId, draft.trim(), replyTo)
            draft = ""
            replyTo = null
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text("SMS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹", color = IOSBlue, style = MaterialTheme.typography.headlineLarge) } },
                actions = { },
            )
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding().imePadding()) {
                if (vm.smsStatus.isNotBlank()) {
                    Text(vm.smsStatus, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall)
                }
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
                        TextButton(modifier = Modifier.liquidGlass(), onClick = { replyTo = null }) { Text("Cancel") }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    TextButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                simOptions = try { context.getSystemService(SubscriptionManager::class.java).activeSubscriptionInfoList.orEmpty() } catch (_: SecurityException) { emptyList() }
                                showSimPicker = true
                            } else simPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                        modifier = Modifier.size(46.dp).liquidGlassCircle(),
                        contentPadding = PaddingValues(0.dp),
                    ) { Text("SIM", color = IOSBlue, style = MaterialTheme.typography.labelSmall) }
                    Spacer(Modifier.width(7.dp))
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Text Message · SMS") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 5,
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.size(50.dp).liquidGlassCircle(),
                        contentPadding = PaddingValues(0.dp),
                        enabled = draft.isNotBlank(),
                        onClick = {
                            val text = draft.trim()
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                vm.sendSms(conversationId, text, replyTo)
                                draft = ""
                                replyTo = null
                            } else {
                                permissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        },
                    ) { Text("↑", style = MaterialTheme.typography.titleLarge) }
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
                            color = if (mine) IOSBlue.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .combinedClickable(onClick = {}, onLongClick = { menuFor = m })
                                .border(
                                    1.dp,
                                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.78f), IOSBlue.copy(alpha = 0.45f), Color.White.copy(alpha = 0.22f))),
                                    RoundedCornerShape(18.dp),
                                ),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        formatTime(m.createdAt) + (if (m.edited) "  edited" else ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = secondary,
                                    )
                                    if (mine) {
                                        Spacer(Modifier.width(5.dp))
                                        val (mark, tint) = when (m.deliveryState) {
                                            "DELIVERED" -> "✓✓" to Color(0xFF8FD3FF)
                                            "SENT_TO_NETWORK" -> "✓" to Color.White.copy(alpha = 0.78f)
                                            "SENDING", "LOCAL" -> "◷" to Color.White.copy(alpha = 0.78f)
                                            "DELIVERY_FAILED", "FAILED", "NO_SERVICE", "RADIO_OFF" -> "!" to Color(0xFFFF6B64)
                                            else -> "✓" to Color.White.copy(alpha = 0.78f)
                                        }
                                        Text(mark, color = tint, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
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

    if (showSimPicker) {
        AlertDialog(
            onDismissRequest = { showSimPicker = false },
            title = { Text("Choose SIM") },
            text = { Column {
                if (simOptions.isEmpty()) Text("No active SIM was found. The system default SIM will be used.")
                simOptions.forEachIndexed { index, info ->
                    TextButton(modifier = Modifier.liquidGlass(), onClick = { vm.setConversationSim(conversationId, info.subscriptionId); showSimPicker = false }) {
                        Text("SIM ${index + 1} · ${info.carrierName}")
                    }
                }
                TextButton(modifier = Modifier.liquidGlass(), onClick = { vm.setConversationSim(conversationId, null); showSimPicker = false }) { Text("Use system default") }
            } },
            confirmButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { showSimPicker = false }) { Text("Close") } },
        )
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
                TextButton(modifier = Modifier.liquidGlass(), onClick = {
                    vm.editMessage(m, editText)
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(modifier = Modifier.liquidGlass(), onClick = { editing = null }) { Text("Cancel") } },
        )
    }
}
