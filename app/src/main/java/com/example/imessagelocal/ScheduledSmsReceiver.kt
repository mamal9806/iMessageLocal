package com.example.imessagelocal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import com.example.imessagelocal.data.AppDatabase
import com.example.imessagelocal.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", -1L)
        if (scheduleId <= 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).chatDao()
                val item = dao.getScheduled(scheduleId) ?: return@launch
                if (item.status != "PENDING") return@launch
                if (context.checkSelfPermission(android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    dao.updateScheduledStatus(scheduleId, "FAILED_PERMISSION")
                    return@launch
                }
                val manager = if (item.simSubscriptionId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SmsManager.getSmsManagerForSubscriptionId(item.simSubscriptionId)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION") SmsManager.getDefault()
                }
                val parts = manager.divideMessage(item.text)
                if (parts.size > 1) manager.sendMultipartTextMessage(item.phoneNumber, null, parts, null, null)
                else manager.sendTextMessage(item.phoneNumber, null, item.text, null, null)
                dao.insertMessage(MessageEntity(conversationId = item.conversationId, text = item.text, isOutgoing = true, deliveryState = "SENT_TO_NETWORK", simSubscriptionId = item.simSubscriptionId))
                dao.getConversation(item.conversationId)?.let {
                    dao.updateConversation(it.copy(lastText = item.text, updatedAt = System.currentTimeMillis()))
                }
                dao.updateScheduledStatus(scheduleId, "SENT")
            } catch (_: Exception) {
                try { AppDatabase.get(context).chatDao().updateScheduledStatus(scheduleId, "FAILED") } catch (_: Exception) { }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
