package com.tionix.rms.feature.filesearch.presentation

import com.tionix.rms.feature.search.domain.model.SearchResult

sealed class FileSearchUiState {
    object Idle : FileSearchUiState()
    object Loading : FileSearchUiState()
    data class Success(val results: List<SearchResult>) : FileSearchUiState()
    data class Error(val message: String) : FileSearchUiState()
    object NotFound : FileSearchUiState()
    object FileDetailLoaded : FileSearchUiState()
}
