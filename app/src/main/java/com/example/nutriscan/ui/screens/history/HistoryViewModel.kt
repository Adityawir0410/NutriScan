package com.example.nutriscan.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.repository.HistoryRepository
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.ScanHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState = _historyState.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            repository.getScanHistory().collect { result ->
                when (result) {
                    is Result.Loading -> _historyState.value = HistoryState.Loading
                    is Result.Success -> {
                        val data = result.data ?: emptyList()
                        if (data.isEmpty()) {
                            _historyState.value = HistoryState.Empty
                        } else {
                            _historyState.value = HistoryState.Success(data)
                        }
                    }
                    is Result.Error -> _historyState.value = HistoryState.Error(result.message ?: "Error")
                }
            }
        }
    }
}

sealed class HistoryState {
    object Loading : HistoryState()
    object Empty : HistoryState()
    data class Success(val data: List<ScanHistory>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}