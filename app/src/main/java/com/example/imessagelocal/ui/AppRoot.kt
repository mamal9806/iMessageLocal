package com.example.imessagelocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
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

    AppBackground(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = Triple(vm.settings.setupComplete, openId, tab),
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 18 }) togetherWith fadeOut(tween(150))
                },
                label = "screen_transition",
            ) { (setupComplete, conversationId, selectedTab) ->
                when {
                    !setupComplete -> SetupScreen(vm) {
                        vm.updateSettings(vm.settings.copy(setupComplete = true))
                    }
                    conversationId != null -> ChatScreen(vm, conversationId, onBack = { vm.open(null) })
                    selectedTab == RootTab.Messages -> ConversationListScreen(vm, onOpen = vm::open, onSettings = { tab = RootTab.Settings })
                    selectedTab == RootTab.Contacts -> ContactsScreen(vm, onOpen = vm::open)
                    else -> SettingsScreen(vm, onBack = { tab = RootTab.Messages })
                }
            }
            if (openId == null && vm.settings.setupComplete) {
                Box(Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                    BottomNav(selected = tab, english = vm.settings.language == 1, onSelect = { tab = it })
                }
            }
        }
    }
}
