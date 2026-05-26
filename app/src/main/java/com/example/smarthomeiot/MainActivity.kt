package com.example.smarthomeiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.smarthomeiot.ui.SmartHomeApp
import com.example.smarthomeiot.ui.theme.SmartHomeTheme
import com.example.smarthomeiot.viewmodel.SmartHomeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<SmartHomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                SmartHomeApp(viewModel = viewModel)
            }
        }
    }
}
