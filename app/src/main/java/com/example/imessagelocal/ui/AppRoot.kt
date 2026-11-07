package com.example.imessagelocal.ui

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppRoot(vm: AppViewModel) {
    val openId by vm.openConversationId.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val id = openId

    BackHandler(enabled = id != null || showSettings) {
        if (id != null) vm.open(null) else showSettings = false
    }

    when {
        id != null -> ChatScreen(vm, id, onBack = { vm.open(null) })
        showSettings -> SettingsScreen(vm, onBack = { showSettings = false })
        else -> ConversationListScreen(
            vm,
            onOpen = { vm.open(it) },
            onSettings = { showSettings = true },
        )
    }
}
