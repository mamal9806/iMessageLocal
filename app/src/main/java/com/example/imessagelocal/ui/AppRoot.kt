package com.example.imessagelocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.align
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class RootTab { Messages, Contacts, Settings }

@Composable
fun AppRoot(vm: AppViewModel) {
    val openId by vm.openConversationId.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(RootTab.Messages) }

    BackHandler(enabled = openId != null) { vm.open(null) }

    AppBackground {
        Box(Modifier.fillMaxSize()) {
            when {
                openId != null -> ChatScreen(vm, openId!!, onBack = { vm.open(null) })
                tab == RootTab.Messages -> ConversationListScreen(vm, onOpen = vm::open, onSettings = { tab = RootTab.Settings })
                tab == RootTab.Contacts -> ContactsScreen(vm, onOpen = vm::open)
                else -> SettingsScreen(vm, onBack = { tab = RootTab.Messages })
            }
            if (openId == null) {
                Box(Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                    BottomNav(selected = tab, onSelect = { tab = it })
                }
            }
        }
    }
}
