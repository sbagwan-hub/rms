package com.tionix.rms.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.history.domain.model.ActionFilter
import com.tionix.rms.feature.history.domain.model.DateFilter
import com.tionix.rms.feature.history.domain.usecase.GetHistoryUseCase
import com.tionix.rms.feature.history.domain.usecase.RetrySyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val retrySyncUseCase: RetrySyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _actionFilter = MutableStateFlow(ActionFilter.ALL)
    val actionFilter: StateFlow<ActionFilter> = _actionFilter.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            val result = getHistoryUseCase(
                actionFilter = _actionFilter.value,
                dateFilter = _dateFilter.value,
                userId = _currentUserId.value
            )
            
            if (result.isSuccess) {
                _uiState.value = HistoryUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = HistoryUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load history")
            }
        }
    }

    fun setActionFilter(filter: ActionFilter) {
        _actionFilter.value = filter
        loadHistory()
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
        loadHistory()
    }

    fun setUserId(userId: String?) {
        _currentUserId.value = userId
        loadHistory()
    }

    fun retrySync(historyItemId: String) {
        viewModelScope.launch {
            val result = retrySyncUseCase(historyItemId)
            if (result.isSuccess) {
                loadHistory()
            } else {
                _uiState.value = HistoryUiState.Error(result.exceptionOrNull()?.message ?: "Failed to retry sync")
            }
        }
    }
}
