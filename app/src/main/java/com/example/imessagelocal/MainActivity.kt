package com.example.imessagelocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.imessagelocal.ui.AppRoot
import com.example.imessagelocal.ui.AppViewModel
import com.example.imessagelocal.ui.IMessageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            intent.getLongExtra("conversation_id", -1L).takeIf { it > 0L }?.let { vm.open(it) }
            val settings = vm.settings
            val dark = when (settings.darkMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            IMessageTheme(darkTheme = dark, fontScale = settings.fontScale) {
                AppRoot(vm)
            }
        }
    }
}
