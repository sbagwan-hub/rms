package com.tionix.rms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tionix.rms.ui.screens.LoginScreen
import com.tionix.rms.ui.screens.ScannerScreen
import com.tionix.rms.ui.screens.TasksScreen
import com.tionix.rms.ui.screens.ProfileScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tionix.rms.ui.dashboard.DashboardScreen
import com.tionix.rms.ui.theme.RMSTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.tionix.rms.ui.viewmodels.MainViewModel
import androidx.compose.runtime.collectAsState

enum class Screen {
    Login, Dashboard, Scanner
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RMSTheme {
                val mainViewModel: MainViewModel = viewModel()
                val currentScreen by mainViewModel.currentScreen.collectAsState()
                val scannedCode by mainViewModel.lastScannedCode.collectAsState()

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { mainViewModel.navigateTo(Screen.Scanner) },
                            onOpenScanner = { mainViewModel.navigateTo(Screen.Scanner) }
                        )
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            scannedCode = scannedCode,
                            onOpenScanner = { mainViewModel.navigateTo(Screen.Scanner) }
                        )
                    }
                    Screen.Scanner -> {
                        ScannerScreen(
                            onCodeScanned = { code -> mainViewModel.onCodeScanned(code) },
                            onManualEntry = { mainViewModel.navigateTo(Screen.Login) },
                            onMenuClick = { mainViewModel.navigateTo(Screen.Dashboard) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun DashboardPreview() {
    RMSTheme {
        DashboardScreen(onOpenScanner = {})
    }
}
