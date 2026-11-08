package com.example.imessagelocal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.EmojiEmotions
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
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.imessagelocal.data.MessageEntity
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(vm: AppViewModel, conversationId: Long, onBack: () -> Unit) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val scheduledMessages by vm.scheduledMessages.collectAsStateWithLifecycle()
    val title = vm.conversationName(conversationId)
    val number = vm.conversationNumber(conversationId)
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var draft by rememberSaveable { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<MessageEntity?>(null) }
    var menuFor by remember { mutableStateOf<MessageEntity?>(null) }
    var confirmDeleteMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editing by remember { mutableStateOf<MessageEntity?>(null) }
    var editText by remember { mutableStateOf("") }
    var simOptions by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
    var showSimPicker by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var showStickerPicker by remember { mutableStateOf(false) }
    val english = vm.settings.language == 1

    val openSchedulePicker = {
        val initial = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
        DatePickerDialog(context, { _, year, month, day ->
            val picked = (initial.clone() as Calendar).apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day) }
            TimePickerDialog(context, { _, hour, minute ->
                picked.set(Calendar.HOUR_OF_DAY, hour); picked.set(Calendar.MINUTE, minute); picked.set(Calendar.SECOND, 0)
                if (picked.timeInMillis > System.currentTimeMillis()) { vm.scheduleSms(conversationId, draft, picked.timeInMillis); draft = "" }
            }, initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), true).show()
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show()
    }
    val schedulePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && draft.isNotBlank()) openSchedulePicker()
    }

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

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, if (english) "Back" else "بازگشت", tint = TextPrimary) }
            Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(title.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (english) "SMS" else "پیامک", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { showStickerPicker = true }) {
                Icon(Icons.Default.EmojiEmotions, if (english) "Stickers" else "استیکرها", tint = IOSBlue)
            }
            Box {
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f))
                        .clickable { showTemplates = true }
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Message, if (english) "Default message" else "پیام پیش‌فرض", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(17.dp))
                    Text(if (vm.settings.language == 1) "Default message" else "پیام پیش‌فرض", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp))
                }
                androidx.compose.material3.DropdownMenu(expanded = showTemplates, onDismissRequest = { showTemplates = false }) {
                    (if (english) listOf("Hi, how are you?", "Please call me.", "I can’t talk right now. I’ll call you later.", "Thanks a lot.", "I’m on my way and will be there soon.") else listOf("سلام، حالت چطوره؟", "لطفاً با من تماس بگیر.", "الان فرصت صحبت ندارم؛ بعداً تماس می‌گیرم.", "ممنون از لطفت.", "در راه هستم و به‌زودی می‌رسم.")).forEach { template ->
                        androidx.compose.material3.DropdownMenuItem(text = { Text(template) }, onClick = { draft = template; showTemplates = false })
                    }
                }
            }
            IconButton(onClick = {
                if (number.isNotBlank()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")))
            }) { Icon(Icons.Default.Call, if (english) "Call" else "تماس", tint = TextSecondary) }
        }

        Box(Modifier.fillMaxSize().weight(1f)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 12.dp)) {
                val pinnedMessages = messages.filter { it.pinned }
                if (pinnedMessages.isNotEmpty()) {
                    item(key = "pinned_messages_header") {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(if (english) "Pinned messages" else "پیام‌های سنجاق‌شده", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            pinnedMessages.forEach { pinned ->
                                Text(pinned.text, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                items(messages, key = { it.id }) { m ->
                    MessageBubble(m, english, onLongPress = { menuFor = m })
                }
            }
        }

        if (vm.smsStatus.isNotBlank()) {
            Text(vm.smsStatus, color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        }
        replyTo?.let { r ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (vm.settings.language == 1) "Replying to" else "پاسخ به", color = IOSBlue, style = MaterialTheme.typography.labelMedium)
                    Text(r.text, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { replyTo = null }) { Text(if (english) "Cancel" else "لغو") }
            }
        }
        if (scheduledMessages.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                scheduledMessages.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = IOSBlue, modifier = Modifier.size(16.dp))
                        Text("  ${java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(item.scheduledAt))} · ${item.text.take(28)}", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), maxLines = 1)
                        TextButton(onClick = { vm.cancelScheduledSms(item.id) }) { Text(if (english) "Cancel" else "لغو", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
        Column(
            Modifier.fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f).widthIn(min = 0.dp),
                    placeholder = { Text(if (vm.settings.language == 1) "Type a message…" else "پیام خود را بنویسید…", color = TextSecondary) },
                    shape = RoundedCornerShape(21.dp),
                    minLines = 2,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                        focusedBorderColor = IOSBlue,
                        cursorColor = IOSBlue,
                    ),
                )
                Spacer(Modifier.width(7.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                simOptions = activeSims(context)
                                showSimPicker = true
                            } else simPermission.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                    ) { Icon(Icons.Default.Smartphone, if (english) "Choose SIM" else "انتخاب سیم‌کارت", tint = IOSBlue) }
                    Text("SIM", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(if (draft.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .combinedClickable(
                                enabled = draft.isNotBlank(),
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                        vm.sendSms(conversationId, draft.trim(), replyTo); draft = ""; replyTo = null
                                    } else smsPermission.launch(Manifest.permission.SEND_SMS)
                                },
                                onLongClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                        schedulePermission.launch(Manifest.permission.SEND_SMS)
                                    } else openSchedulePicker()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Default.Send, if (english) "Send; hold to schedule" else "ارسال؛ برای زمان‌بندی چند لحظه نگه دارید", tint = if (draft.isNotBlank()) MaterialTheme.colorScheme.onPrimary else TextSecondary) }
                    Text(if (vm.settings.language == 1) "Send" else "ارسال", color = if (draft.isNotBlank()) IOSBlue else TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (showStickerPicker) {
        val stickers = listOf("😀", "😂", "🥰", "😍", "😎", "😭", "🤔", "👍", "❤️", "🎉", "🔥", "🙏")
        AlertDialog(
            onDismissRequest = { showStickerPicker = false },
            title = { Text(if (english) "Choose a sticker" else "انتخاب استیکر") },
            text = {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.height(190.dp),
                ) {
                    items(stickers.size) { index ->
                        TextButton(onClick = { draft = stickers[index]; showStickerPicker = false }) {
                            Text(stickers[index], fontSize = 30.sp)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showStickerPicker = false }) { Text(if (english) "Close" else "بستن") } },
        )
    }

    menuFor?.let { m ->
        AlertDialog(
            onDismissRequest = { menuFor = null },
            title = { Text(if (english) "Message actions" else "عملیات پیام") },
            text = { Text(m.text, maxLines = 5, overflow = TextOverflow.Ellipsis) },
            confirmButton = {
                TextButton(onClick = { replyTo = m; menuFor = null }) { Text(if (english) "Reply" else "پاسخ") }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { vm.toggleMessagePin(m); menuFor = null }) {
                        Text(if (m.pinned) if (english) "Unpin message" else "برداشتن سنجاق" else if (english) "Pin message" else "سنجاق کردن پیام")
                    }
                    if (m.isOutgoing) {
                        TextButton(onClick = { editText = m.text; editing = m; menuFor = null }) { Text(if (english) "Edit" else "ویرایش") }
                    }
                    TextButton(onClick = { confirmDeleteMessage = m; menuFor = null }) {
                        Text(if (english) "Delete message" else "حذف پیام", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
        )
    }

    confirmDeleteMessage?.let { m ->
        AlertDialog(
            onDismissRequest = { confirmDeleteMessage = null },
            title = { Text(if (english) "Delete message?" else "حذف پیام؟") },
            text = { Text(if (english) "This message will be removed from A SMS only. It cannot be deleted from the recipient’s phone." else "این پیام از گفتگوی داخل A SMS حذف می‌شود. پیامک ارسال‌شده از گوشی گیرنده حذف نخواهد شد.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteMessage(m); confirmDeleteMessage = null }) {
                    Text(if (english) "Delete" else "حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteMessage = null }) { Text(if (english) "Cancel" else "انصراف") } },
        )
    }

    editing?.let { m ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (english) "Edit message" else "ویرایش پیام") },
            text = { OutlinedTextField(editText, { editText = it }, singleLine = false) },
            confirmButton = { TextButton(onClick = { vm.editMessage(m, editText); editing = null }) { Text(if (english) "Save" else "ذخیره") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(if (english) "Cancel" else "لغو") } },
        )
    }

    if (showSimPicker) {
        AlertDialog(
            onDismissRequest = { showSimPicker = false },
            title = { Text(if (english) "Choose SIM" else "انتخاب سیم‌کارت") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { vm.setConversationSim(conversationId, null); showSimPicker = false }) { Text(if (english) "System default SIM" else "سیم‌کارت پیش‌فرض سیستم") }
                    simOptions.forEachIndexed { index, info ->
                        TextButton(onClick = { vm.setConversationSim(conversationId, info.subscriptionId); showSimPicker = false }) {
                            Text("SIM ${index + 1} · ${info.carrierName ?: if (english) "Carrier" else "اپراتور"}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSimPicker = false }) { Text(if (english) "Cancel" else "لغو") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(m: MessageEntity, english: Boolean, onLongPress: () -> Unit) {
    // The selected accent colors the bubble surface and its outline, never the message text.
    val shape = if (m.isOutgoing) {
        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 7.dp, bottomEnd = 22.dp)
    } else {
        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 7.dp)
    }
    val accent = MaterialTheme.colorScheme.primary
    val bubbleColor = if (m.isOutgoing) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.isOutgoing) Arrangement.Start else Arrangement.End) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp).combinedClickable(onClick = {}, onLongClick = onLongPress),
            horizontalAlignment = if (m.isOutgoing) Alignment.Start else Alignment.End,
        ) {
            val isSticker = m.text.codePointCount(0, m.text.length) <= 4 && m.text.any { it.code > 0x1F000 }
            var stickerVisible by remember(m.id) { mutableStateOf(false) }
            LaunchedEffect(m.id) { stickerVisible = true }
            val stickerScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (stickerVisible) 1f else 0.45f,
                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.52f, stiffness = 360f),
                label = "sticker_pop",
            )
            Box(
                Modifier
                    .graphicsLayer {
                        if (isSticker) {
                            scaleX = stickerScale
                            scaleY = stickerScale
                            rotationZ = (1f - stickerScale) * -12f
                        }
                    }
                    .clip(if (isSticker) RoundedCornerShape(18.dp) else shape)
                    .background(bubbleColor)
                    .border(
                        width = if (m.isOutgoing) 1.4.dp else 0.7.dp,
                        color = if (m.isOutgoing) accent.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                        shape = shape,
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            ) {
                Text(m.text, color = MaterialTheme.colorScheme.onSurface, style = if (isSticker) MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp) else MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
                if (m.pinned) Icon(Icons.Default.PushPin, null, tint = accent, modifier = Modifier.size(12.dp))
                Text(formatTime(m.createdAt), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                if (m.isOutgoing) Text("  ${deliveryLabel(m.deliveryState, english)}", color = if (m.deliveryState == "DELIVERED") SuccessGreen else TextSecondary, style = MaterialTheme.typography.labelSmall)
                if (m.edited) Text(if (english) " · edited" else " · ویرایش‌شده", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun deliveryLabel(s: String, english: Boolean = false) = when (s) {
    "SENDING" -> if (english) "Sending" else "در حال ارسال"
    "SENT_TO_NETWORK" -> if (english) "Sent" else "ارسال شد"
    "DELIVERED" -> if (english) "Delivered" else "تحویل شد"
    "DELIVERY_FAILED", "FAILED" -> if (english) "Failed" else "ناموفق"
    "RECEIVED" -> if (english) "Received" else "دریافت شد"
    else -> ""
}

