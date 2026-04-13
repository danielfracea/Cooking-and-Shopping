package com.cooking.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cooking.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _unitSystem = MutableStateFlow(repository.getUnitSystem())
    val unitSystem: StateFlow<String> = _unitSystem.asStateFlow()

    fun setUnitSystem(system: String) {
        _unitSystem.value = system
        repository.setUnitSystem(system)
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repository) as T
    }
}
