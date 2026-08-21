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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.search.domain.model.SearchResult
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val searchByBarcodeUseCase: SearchByBarcodeUseCase,
    private val getBoxDetailUseCase: GetBoxDetailUseCase,
    private val insertFileUseCase: com.tionix.rms.feature.search.domain.usecase.InsertFileUseCase,
    val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase,
    private val beepPlayer: BeepPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _boxDetail = MutableStateFlow<BoxDetail?>(null)
    val boxDetail: StateFlow<BoxDetail?> = _boxDetail.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>()
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

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

    fun getBoxDetail(boxId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: BoxDetail\nAPI request started")
            } else {
                _uiState.value = SearchUiState.Loading
            }
            
            val result = getBoxDetailUseCase(boxId)
            
            if (result.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: BoxDetail\nAPI response: 200\nState updated")
                val detail = result.getOrNull()
                _boxDetail.value = detail
                _uiState.value = SearchUiState.BoxDetailLoaded
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Failed to load box details"
                if (isRefresh && _boxDetail.value != null) {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                } else {
                    _uiState.value = SearchUiState.Error(errMsg)
                }
            }
            _isRefreshing.value = false
            if (isRefresh) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: BoxDetail\nRefresh completed")
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

    private val _insertMessage = MutableStateFlow<String?>(null)
    val insertMessage: StateFlow<String?> = _insertMessage.asStateFlow()

    fun insertFile(boxId: String, fileBarcode: String, title: String? = null, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = insertFileUseCase(boxId, fileBarcode, title)
            if (result.isSuccess) {
                val msg = result.getOrNull() ?: "File inserted successfully"
                _insertMessage.value = msg
                // Refresh box details to show updated contents
                getBoxDetail(boxId)
                onComplete(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to insert file"
                _insertMessage.value = err
                onComplete(false, err)
            }
        }
    }

    suspend fun validateAndLookupBarcode(barcode: String): Result<SearchResult?> {
        return searchByBarcodeUseCase(barcode)
    }

    fun playPositiveBeep() {
        beepPlayer.positive()
    }

    fun playErrorBeep() {
        beepPlayer.error()
    }

    fun clearInsertMessage() {
        _insertMessage.value = null
    }

    fun clearBoxDetail() {
        _boxDetail.value = null
    }
}
