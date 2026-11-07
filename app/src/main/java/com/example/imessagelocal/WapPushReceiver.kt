package com.example.imessagelocal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** MMS/WAP push is registered for SMS-role eligibility; full MMS transport is not implemented. */
class WapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { /* MMS requires a separate PDU/HTTP transport. */ }
}
