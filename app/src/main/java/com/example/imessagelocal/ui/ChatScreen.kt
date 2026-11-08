package com.example.imessagelocal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.MessageEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(vm: AppViewModel, conversationId: Long, onBack: () -> Unit) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val title = vm.conversationName(conversationId)
    val number = vm.conversationNumber(conversationId)
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var draft by rememberSaveable { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<MessageEntity?>(null) }
    var menuFor by remember { mutableStateOf<MessageEntity?>(null) }
    var editing by remember { mutableStateOf<MessageEntity?>(null) }
    var editText by remember { mutableStateOf("") }
    var simOptions by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
    var showSimPicker by remember { mutableStateOf(false) }

    val simPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            simOptions = activeSims(context)
            showSimPicker = true
        }
    }
    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && draft.isNotBlank()) {
            vm.sendSms(conversationId, draft.trim(), replyTo)
            draft = ""; replyTo = null
        }
    }

    LaunchedEffect(messages.size) {
        vm.markConversationRead(conversationId)
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت", tint = TextPrimary) }
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF3574B8)), contentAlignment = Alignment.Center) {
                Text(title.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("پیامک", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = {
                if (number.isNotBlank()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")))
            }) { Icon(Icons.Default.Call, "تماس", tint = TextSecondary) }
        }

        Box(Modifier.fillMaxSize().weight(1f)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 12.dp)) {
                items(messages, key = { it.id }) { m ->
                    MessageBubble(m, onLongPress = { menuFor = m })
                }
            }
        }

        if (vm.smsStatus.isNotBlank()) {
            Text(vm.smsStatus, color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        }
        replyTo?.let { r ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("پاسخ به", color = IOSBlue, style = MaterialTheme.typography.labelMedium)
                    Text(r.text, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { replyTo = null }) { Text("لغو") }
            }
        }
        Row(
            Modifier.fillMaxWidth().imePadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        simOptions = activeSims(context)
                        showSimPicker = true
                    } else simPermission.launch(Manifest.permission.READ_PHONE_STATE)
                },
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Icon(Icons.Default.Call, contentDescription = "سیم‌کارت", modifier = Modifier.size(18.dp))
                Text("SIM", modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("پیامک…", color = TextSecondary) },
                shape = RoundedCornerShape(25.dp),
                maxLines = 5,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = IOSBlue,
                ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        vm.sendSms(conversationId, draft.trim(), replyTo); draft = ""; replyTo = null
                    } else smsPermission.launch(Manifest.permission.SEND_SMS)
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.size(52.dp).clip(CircleShape).background(if (draft.isNotBlank()) IOSBlue else Color.White.copy(alpha = .08f)),
            ) { Icon(Icons.Default.Send, "ارسال", tint = Color.White) }
        }
    }

    menuFor?.let { m ->
        AlertDialog(
            onDismissRequest = { menuFor = null },
            title = { Text("پیام") },
            text = { Text(m.text) },
            confirmButton = {
                TextButton(onClick = { replyTo = m; menuFor = null }) { Text("پاسخ") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { editText = m.text; editing = m; menuFor = null }) { Text("ویرایش") }
                    TextButton(onClick = { vm.deleteMessage(m); menuFor = null }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                }
            },
        )
    }

    editing?.let { m ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("ویرایش پیام") },
            text = { OutlinedTextField(editText, { editText = it }, singleLine = false) },
            confirmButton = { TextButton(onClick = { vm.editMessage(m, editText); editing = null }) { Text("ذخیره") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("لغو") } },
        )
    }

    if (showSimPicker) {
        AlertDialog(
            onDismissRequest = { showSimPicker = false },
            title = { Text("انتخاب سیم‌کارت") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { vm.setConversationSim(conversationId, null); showSimPicker = false }) { Text("سیم‌کارت پیش‌فرض سیستم") }
                    simOptions.forEachIndexed { index, info ->
                        TextButton(onClick = { vm.setConversationSim(conversationId, info.subscriptionId); showSimPicker = false }) {
                            Text("SIM ${index + 1} · ${info.carrierName ?: "اپراتور"}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSimPicker = false }) { Text("لغو") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(m: MessageEntity, onLongPress: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.isOutgoing) Arrangement.Start else Arrangement.End) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp).combinedClickable(onClick = {}, onLongClick = onLongPress),
            horizontalAlignment = if (m.isOutgoing) Alignment.Start else Alignment.End,
        ) {
            Box(
                Modifier
                    .clip(shape)
                    .background(if (m.isOutgoing) BrushBubbleBlue else BrushBubbleGray)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            ) {
                Text(m.text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                Text(formatTime(m.createdAt), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                if (m.isOutgoing) Text("  ${deliveryLabel(m.deliveryState)}", color = if (m.deliveryState == "DELIVERED") SuccessGreen else TextSecondary, style = MaterialTheme.typography.labelSmall)
                if (m.edited) Text(" · ویرایش‌شده", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private val BrushBubbleBlue = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF087AF3), Color(0xFF1551C7)))
private val BrushBubbleGray = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF202A40), Color(0xFF161D30)))
private fun deliveryLabel(s: String) = when (s) { "SENDING" -> "در حال ارسال"; "SENT_TO_NETWORK" -> "ارسال شد"; "DELIVERED" -> "تحویل شد"; "DELIVERY_FAILED", "FAILED" -> "ناموفق"; "RECEIVED" -> "دریافت شد"; else -> "" }

