package com.tionix.rms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tionix.rms.core.network.AuthEventBus
import com.tionix.rms.ui.navigation.RmsNavGraph
import com.tionix.rms.ui.theme.RMSTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authEventBus: AuthEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RMSTheme {
                RmsNavGraph(authEventBus = authEventBus)
            }
        }
    }
}
