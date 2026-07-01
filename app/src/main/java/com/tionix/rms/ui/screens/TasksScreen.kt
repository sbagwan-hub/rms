package com.tionix.rms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tionix.rms.ui.dashboard.RmsTopAppBar
import com.tionix.rms.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    Scaffold(
        topBar = {
            RmsTopAppBar(
                title = "Tasks",
                actions = {
                    IconButton(onClick = { /* Add Task */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tasks Screen",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
