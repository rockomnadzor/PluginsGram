package com.github.pluginsgram.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.pluginsgram.BuildConfig
import com.github.pluginsgram.tdlib.TdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AuthScreenState {
    object Loading : AuthScreenState()
    object WaitPhoneNumber : AuthScreenState()
    object WaitCode : AuthScreenState()
    object Ready : AuthScreenState()
    data class Error(val message: String) : AuthScreenState()
}

data class ChatItem(
    val id: Long,
    val title: String,
    val lastMessage: String
)

class TdViewModel(app: Application) : AndroidViewModel(app) {

    private val tdManager = TdManager(
        apiId = BuildConfig.TD_API_ID,
        apiHash = BuildConfig.TD_API_HASH,
        databaseDirectory = app.filesDir.absolutePath + "/tdlib"
    )

    private val _authState = MutableStateFlow<AuthScreenState>(AuthScreenState.Loading)
    val authState: StateFlow<AuthScreenState> = _authState.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatItem>>(emptyList())
    val chats: StateFlow<List<ChatItem>> = _chats.asStateFlow()

    private val chatOrder = mutableListOf<Long>()
    private val chatMap = mutableMapOf<Long, ChatItem>()

    init {
        viewModelScope.launch {
            tdManager.updates().collect { update -> handleUpdate(update) }
        }
        tdManager.start()
    }

    private fun handleUpdate(update: JSONObject) {
        when (update.optString("@type")) {
            "updateAuthorizationState" -> {
                val state = update.getJSONObject("authorization_state")
                when (state.optString("@type")) {
                    "authorizationStateWaitPhoneNumber" -> _authState.value = AuthScreenState.WaitPhoneNumber
                    "authorizationStateWaitCode" -> _authState.value = AuthScreenState.WaitCode
                    "authorizationStateReady" -> {
                        _authState.value = AuthScreenState.Ready
                        tdManager.loadChats()
                    }
                }
            }
            "updateNewChat" -> upsertChat(update.getJSONObject("chat"))
            "updateChatLastMessage" -> {
                val chatId = update.getLong("chat_id")
                val text = extractMessageText(update.optJSONObject("last_message"))
                chatMap[chatId]?.let {
                    chatMap[chatId] = it.copy(lastMessage = text)
                    publishChats()
                }
            }
            "error" -> _authState.value = AuthScreenState.Error(update.optString("message", "Неизвестная ошибка"))
        }
    }

    private fun upsertChat(chat: JSONObject) {
        val id = chat.getLong("id")
        val title = chat.optString("title", "Без названия")
        val lastMessage = extractMessageText(chat.optJSONObject("last_message"))
        if (!chatMap.containsKey(id)) chatOrder.add(id)
        chatMap[id] = ChatItem(id, title, lastMessage)
        publishChats()
    }

    private fun extractMessageText(message: JSONObject?): String {
        val content = message?.optJSONObject("content") ?: return ""
        return when (content.optString("@type")) {
            "messageText" -> content.optJSONObject("text")?.optString("text", "") ?: ""
            "messagePhoto" -> "\uD83D\uDCF7 Фото"
            "messageVideo" -> "\uD83C\uDFA5 Видео"
            "messageSticker" -> "Стикер"
            else -> ""
        }
    }

    private fun publishChats() {
        _chats.value = chatOrder.mapNotNull { chatMap[it] }
    }

    fun submitPhone(phone: String) = tdManager.sendPhoneNumber(phone)
    fun submitCode(code: String) = tdManager.sendCode(code)
}
