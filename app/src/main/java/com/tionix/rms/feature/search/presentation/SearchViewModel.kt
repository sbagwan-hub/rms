package com.tionix.rms.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.search.domain.repository.SearchType
import com.tionix.rms.feature.search.domain.usecase.SearchByBarcodeUseCase
import com.tionix.rms.feature.search.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val searchByBarcodeUseCase: SearchByBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    fun onSearchTypeChanged(type: SearchType) {
        _searchType.value = type
    }

    fun search() {
        viewModelScope.launch {
            if (_searchQuery.value.isBlank()) {
                _uiState.value = SearchUiState.Idle
                return@launch
            }
            
            _uiState.value = SearchUiState.Loading
            val result = searchUseCase(_searchQuery.value, _searchType.value)
            
            if (result.isSuccess) {
                _uiState.value = SearchUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = SearchUiState.Error(result.exceptionOrNull()?.message ?: "Search failed")
            }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val result = searchByBarcodeUseCase(barcode)
            
            if (result.isSuccess) {
                val searchResult = result.getOrNull()
                if (searchResult != null) {
                    _uiState.value = SearchUiState.Success(listOf(searchResult))
                } else {
                    _uiState.value = SearchUiState.Error("No results found")
                }
            } else {
                _uiState.value = SearchUiState.Error(result.exceptionOrNull()?.message ?: "Barcode search failed")
            }
        }
    }
}
