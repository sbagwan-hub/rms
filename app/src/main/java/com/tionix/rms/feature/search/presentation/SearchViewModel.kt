package com.tionix.rms.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.search.domain.model.BoxDetail
import com.tionix.rms.feature.search.domain.repository.SearchType
import com.tionix.rms.feature.search.domain.usecase.GetBoxDetailUseCase
import com.tionix.rms.feature.search.domain.usecase.SearchByBarcodeUseCase
import com.tionix.rms.feature.search.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val searchByBarcodeUseCase: SearchByBarcodeUseCase,
    private val getBoxDetailUseCase: GetBoxDetailUseCase,
    private val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _boxDetail = MutableStateFlow<BoxDetail?>(null)
    val boxDetail: StateFlow<BoxDetail?> = _boxDetail.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Debounced search (400ms)
        _searchQuery
            .debounce(400)
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _uiState.value = SearchUiState.Idle
                }
            }
            .launchIn(viewModelScope)

        // Collect scanner results
        viewModelScope.launch {
            scannerRepository.scanResults.collect { result ->
                searchByBarcode(result.barcode)
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    fun onSearchTypeChanged(type: SearchType) {
        _searchType.value = type
        if (_searchQuery.value.isNotBlank()) {
            performSearch(_searchQuery.value)
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val result = searchUseCase(query, _searchType.value)
            
            if (result.isSuccess) {
                val results = result.getOrNull() ?: emptyList()
                _uiState.value = if (results.isEmpty()) {
                    SearchUiState.NotFound
                } else {
                    SearchUiState.Success(results)
                }
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
                    _uiState.value = SearchUiState.NotFound
                }
            } else {
                _uiState.value = SearchUiState.Error(result.exceptionOrNull()?.message ?: "Barcode search failed")
            }
        }
    }

    fun getBoxDetail(boxId: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val result = getBoxDetailUseCase(boxId)
            
            if (result.isSuccess) {
                _boxDetail.value = result.getOrNull()
                _uiState.value = SearchUiState.BoxDetailLoaded
            } else {
                _uiState.value = SearchUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load box details")
            }
        }
    }

    fun startScanner() {
        viewModelScope.launch {
            initializeScannerUseCase()
            startScanningUseCase()
        }
    }

    fun stopScanner() {
        viewModelScope.launch {
            stopScanningUseCase()
        }
    }

    fun clearBoxDetail() {
        _boxDetail.value = null
    }
}
