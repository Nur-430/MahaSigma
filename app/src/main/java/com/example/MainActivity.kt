package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppContainer
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MahaSigmaTheme
import com.example.ui.viewmodel.MahaSigmaViewModel

class MainActivity : ComponentActivity() {
    private var currentTab by mutableIntStateOf(-1)
    private var currentAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        extractIntentExtras(intent)

        setContent {
            val viewModel: MahaSigmaViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MahaSigmaTheme(darkTheme = isDark) {
                MainAppContainer(
                    viewModel = viewModel,
                    initialTab = if (currentTab in 0..4) currentTab else null,
                    initialAction = currentAction
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractIntentExtras(intent)
    }

    private fun extractIntentExtras(intent: Intent?) {
        if (intent == null) return
        val tab = intent.getIntExtra("EXTRA_TAB", -1)
        if (tab in 0..4) {
            currentTab = tab
        }
        currentAction = intent.getStringExtra("EXTRA_ACTION")
    }
}

