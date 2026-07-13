package com.tionix.rms.feature.search.presentation

import com.tionix.rms.feature.search.domain.model.SearchResult

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<SearchResult>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    object NotFound : SearchUiState()
    object BoxDetailLoaded : SearchUiState()
}
