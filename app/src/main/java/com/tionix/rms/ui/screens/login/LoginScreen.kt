package com.tionix.rms.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tionix.rms.ui.components.PrimaryButton
import com.tionix.rms.ui.components.RMSTextField
import com.tionix.rms.ui.components.SecondaryButton
import com.tionix.rms.ui.theme.Background

@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit
) {
    var operatorId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to Enterprise RMS",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            RMSTextField(
                value = operatorId,
                onValueChange = { operatorId = it },
                label = "Operator ID / Barcode"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RMSTextField(
                value = password,
                onValueChange = { password = it },
                label = "PIN / Password"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PrimaryButton(
                text = "Sign In",
                onClick = onNavigateToDashboard
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecondaryButton(
                text = "Scan Badge",
                onClick = { /* Handle scanner intent */ }
            )
        }
    }
}
