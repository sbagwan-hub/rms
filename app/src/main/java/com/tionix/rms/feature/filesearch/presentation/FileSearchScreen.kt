package com.tionix.rms.feature.filesearch.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.search.domain.model.SearchResult
import com.tionix.rms.ui.common.EmptyState
import com.tionix.rms.ui.common.LoadingState
import com.tionix.rms.ui.common.NotFoundState
import com.tionix.rms.ui.common.OfflineState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSearchScreen(
    onBack: () -> Unit,
    onResultClick: (SearchResult) -> Unit,
    viewModel: FileSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startScanner()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScanner()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        label = { Text("Search Files") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.searchByBarcode(searchQuery) }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                            }
                        }
                    )
                }
            }
            
            if (isOffline) {
                OfflineState()
            }
            
            when (val state = uiState) {
                is FileSearchUiState.Loading -> {
                    LoadingState()
                }
                is FileSearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        NotFoundState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.results.filterIsInstance<SearchResult.FileRecordResult>()) { result ->
                                FileSearchResultCard(
                                    result = result,
                                    onClick = { onResultClick(result) }
                                )
                            }
                        }
                    }
                }
                is FileSearchUiState.NotFound -> {
                    NotFoundState()
                }
                is FileSearchUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                FileSearchUiState.Idle -> {
                    EmptyState(
                        message = "Enter a search query or scan a file barcode",
                        modifier = Modifier
                    )
                }
                FileSearchUiState.FileDetailLoaded -> {
                    // Should not happen in FileSearchScreen, handled in FileDetailScreen
                }
            }
        }
    }
}

@Composable
private fun FileSearchResultCard(
    result: SearchResult.FileRecordResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.barcode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Box: ${result.boxBarcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Location: ${result.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
