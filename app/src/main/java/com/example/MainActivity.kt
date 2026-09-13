package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppContainer
import com.example.ui.theme.MahaSigmaTheme
import com.example.ui.viewmodel.MahaSigmaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MahaSigmaTheme {
                val viewModel: MahaSigmaViewModel = viewModel()
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}
