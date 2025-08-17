package com.example.todoapp.presentation

import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.TaskStatistics

data class TodoUiState(
    val tasks: List<TodoTask> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val showCompletedTasks: Boolean = true,
    val statistics: TaskStatistics = TaskStatistics(),
    val errorMessage: String? = null
)