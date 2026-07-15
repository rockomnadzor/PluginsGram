package com.github.pluginsgram.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.pluginsgram.ui.screens.ChatListScreen
import com.github.pluginsgram.ui.screens.CodeInputScreen
import com.github.pluginsgram.ui.screens.ErrorScreen
import com.github.pluginsgram.ui.screens.LoadingScreen
import com.github.pluginsgram.ui.screens.PhoneInputScreen

class MainActivity : ComponentActivity() {

    private val viewModel: TdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PluginsGramRoot(viewModel)
                }
            }
        }
    }
}

@Composable
fun PluginsGramRoot(viewModel: TdViewModel) {
    val authState by viewModel.authState.collectAsState()
    val chats by viewModel.chats.collectAsState()

    when (val state = authState) {
        is AuthScreenState.Loading -> LoadingScreen()
        is AuthScreenState.WaitPhoneNumber -> PhoneInputScreen(onSubmit = viewModel::submitPhone)
        is AuthScreenState.WaitCode -> CodeInputScreen(onSubmit = viewModel::submitCode)
        is AuthScreenState.Ready -> ChatListScreen(chats)
        is AuthScreenState.Error -> ErrorScreen(state.message)
    }
}
