package com.tionix.rms.feature.filesearch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.filesearch.domain.usecase.GetFileDetailUseCase
import com.tionix.rms.feature.filesearch.domain.usecase.SearchFileByBarcodeUseCase
import com.tionix.rms.feature.filesearch.domain.usecase.SearchFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileSearchViewModel @Inject constructor(
    private val searchFilesUseCase: SearchFilesUseCase,
    private val searchFileByBarcodeUseCase: SearchFileByBarcodeUseCase,
    private val getFileDetailUseCase: GetFileDetailUseCase,
    val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileSearchUiState>(FileSearchUiState.Idle)
    val uiState: StateFlow<FileSearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fileDetail = MutableStateFlow<FileDetail?>(null)
    val fileDetail: StateFlow<FileDetail?> = _fileDetail.asStateFlow()

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
                    _uiState.value = FileSearchUiState.Idle
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

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = FileSearchUiState.Loading
            val result = searchFilesUseCase(query)
            
            if (result.isSuccess) {
                val results = result.getOrNull() ?: emptyList()
                _uiState.value = if (results.isEmpty()) {
                    FileSearchUiState.NotFound
                } else {
                    FileSearchUiState.Success(results)
                }
            } else {
                _uiState.value = FileSearchUiState.Error(result.exceptionOrNull()?.message ?: "Search failed")
            }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.value = FileSearchUiState.Loading
            val result = searchFileByBarcodeUseCase(barcode)
            
            if (result.isSuccess) {
                val searchResult = result.getOrNull()
                if (searchResult != null) {
                    _uiState.value = FileSearchUiState.Success(listOf(searchResult))
                } else {
                    _uiState.value = FileSearchUiState.NotFound
                }
            } else {
                _uiState.value = FileSearchUiState.Error(result.exceptionOrNull()?.message ?: "Barcode search failed")
            }
        }
    }

    fun getFileDetail(fileId: String) {
        viewModelScope.launch {
            _uiState.value = FileSearchUiState.Loading
            val result = getFileDetailUseCase(fileId)
            
            if (result.isSuccess) {
                _fileDetail.value = result.getOrNull()
                _uiState.value = FileSearchUiState.FileDetailLoaded
            } else {
                _uiState.value = FileSearchUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load file details")
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

    fun clearFileDetail() {
        _fileDetail.value = null
    }
}
