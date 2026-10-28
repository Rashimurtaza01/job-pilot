package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.jobpilot.ui.navigation.MainAppContainer
import com.example.jobpilot.ui.viewmodel.JobPilotViewModel
import com.example.jobpilot.util.configureAppCheck
import com.example.ui.theme.JobPilotTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JobPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check attestation configuration with launch intent
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            JobPilotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}
