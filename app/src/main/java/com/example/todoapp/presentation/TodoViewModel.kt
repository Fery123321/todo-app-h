package com.example.todoapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.notification.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val reminderScheduler: TaskReminderScheduler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    private var allTasks: List<TodoTask> = emptyList()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllTasks()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { tasks ->
                    allTasks = tasks
                    val filteredTasks = applyFilters(tasks)
                    _uiState.value = _uiState.value.copy(
                        tasks = filteredTasks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    private fun applyFilters(tasks: List<TodoTask>): List<TodoTask> {
        val currentState = _uiState.value
        var filteredTasks = tasks
        
        // Apply search filter
        if (currentState.searchQuery.isNotBlank()) {
            filteredTasks = filteredTasks.filter { task ->
                task.title.contains(currentState.searchQuery, ignoreCase = true) ||
                task.description.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply category filter
        currentState.selectedCategory?.let { category ->
            filteredTasks = filteredTasks.filter { task ->
                task.category == category
            }
        }
        
        // Apply completion status filter
        if (!currentState.showCompletedTasks) {
            filteredTasks = filteredTasks.filter { task ->
                !task.isCompleted
            }
        }
        
        return filteredTasks
    }
    
    private fun updateFilteredTasks() {
        val filteredTasks = applyFilters(allTasks)
        _uiState.value = _uiState.value.copy(tasks = filteredTasks)
    }
    
    fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.PERSONAL,
        dueDate: LocalDateTime? = null
    ) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Task title cannot be empty"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                val newTask = TodoTask(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority,
                    category = category,
                    dueDate = dueDate
                )
                repository.insertTask(newTask)
                
                // Schedule reminder if task has due date
                if (newTask.dueDate != null) {
                    reminderScheduler.scheduleTaskReminder(newTask)
                }
                
                clearError()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to create task"
                )
            }
        }
    }
    
    fun updateTask(task: TodoTask) {
        if (task.title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Task title cannot be empty"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                repository.updateTask(task)
                
                // Reschedule reminder for updated task
                reminderScheduler.rescheduleTaskReminder(task)
                
                clearError()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to update task"
                )
            }
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                
                // Cancel any scheduled reminder for this task
                reminderScheduler.cancelTaskReminder(taskId)
                
                clearError()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to delete task"
                )
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            try {
                repository.toggleTaskCompletion(taskId)
                
                // Find the task to check if it's now completed
                val task = allTasks.find { it.id == taskId }
                if (task != null) {
                    if (task.isCompleted) {
                        // Task was marked incomplete, reschedule reminder if it has due date
                        reminderScheduler.scheduleTaskReminder(task)
                    } else {
                        // Task was marked complete, cancel reminder
                        reminderScheduler.cancelTaskReminder(taskId)
                    }
                }
                
                clearError()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to toggle task completion"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Search and Filter Methods
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredTasks()
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        updateFilteredTasks()
    }
    
    fun selectCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        updateFilteredTasks()
    }
    
    fun toggleShowCompletedTasks() {
        val currentValue = _uiState.value.showCompletedTasks
        _uiState.value = _uiState.value.copy(showCompletedTasks = !currentValue)
        updateFilteredTasks()
    }
    
    fun setShowCompletedTasks(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCompletedTasks = show)
        updateFilteredTasks()
    }
    
    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategory = null,
            showCompletedTasks = true
        )
        updateFilteredTasks()
    }
    
    fun getFilteredTasksCount(): Int {
        return _uiState.value.tasks.size
    }
    
    fun getTotalTasksCount(): Int {
        return allTasks.size
    }
}