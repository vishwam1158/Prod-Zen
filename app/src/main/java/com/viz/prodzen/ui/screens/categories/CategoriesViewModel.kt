package com.viz.prodzen.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.local.entities.AppCategory
import com.viz.prodzen.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<AppCategory> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateCategoryLimit(categoryId: Int, limitMinutes: Int) {
        viewModelScope.launch {
            categoryRepository.updateCategoryLimit(categoryId, limitMinutes)
        }
    }
}

