package com.github.pluginsgram.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.pluginsgram.core.ChatItem

@Composable
fun ChatListScreen(chats: List<ChatItem>) {
    if (chats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Загрузка чатов...")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ListItem(
                headlineContent = { Text(chat.title) },
                supportingContent = { Text(chat.lastMessage, maxLines = 1) }
            )
            HorizontalDivider()
        }
    }
}
