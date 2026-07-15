package com.github.pluginsgram.tdlib

import io.github.up9cloud.td.JsonClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Ядро логики: обёртка над TDLib через современный JSON-интерфейс (tdjson).
 * Никакого UI. Только связь с серверами Telegram через MTProto (внутри TDLib).
 */
class TdManager(
    private val apiId: Int,
    private val apiHash: String,
    private val databaseDirectory: String
) {
    private val clientId: Int = JsonClient.td_create_client_id()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updates(): Flow<JSONObject> = callbackFlow {
        val job = scope.launch {
            while (true) {
                val raw = JsonClient.td_receive(1.0) ?: continue
                trySend(JSONObject(raw))
            }
        }
        awaitClose { job.cancel() }
    }

    fun send(request: JSONObject) {
        JsonClient.td_send(clientId, request.toString())
    }

    fun start() {
        val params = JSONObject().apply {
            put("@type", "setTdlibParameters")
            put("database_directory", databaseDirectory)
            put("use_message_database", true)
            put("use_secret_chats", true)
            put("api_id", apiId)
            put("api_hash", apiHash)
            put("system_language_code", "ru")
            put("device_model", "PluginsGram")
            put("application_version", "0.1")
        }
        send(params)
    }

    fun sendPhoneNumber(phone: String) {
        send(JSONObject().apply {
            put("@type", "setAuthenticationPhoneNumber")
            put("phone_number", phone)
        })
    }

    fun sendCode(code: String) {
        send(JSONObject().apply {
            put("@type", "checkAuthenticationCode")
            put("code", code)
        })
    }
}
