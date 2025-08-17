package com.example.todoapp.presentation

import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.domain.model.Category

data class TodoUiState(
    val tasks: List<TodoTask> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val showCompletedTasks: Boolean = true,
    val errorMessage: String? = null
)