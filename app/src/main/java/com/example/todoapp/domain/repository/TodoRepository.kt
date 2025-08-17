package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.DailyProgress
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TaskStatistics
import com.example.todoapp.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TodoRepository {
    fun getAllTasks(): Flow<List<TodoTask>>
    fun getTasksByCategory(category: Category): Flow<List<TodoTask>>
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TodoTask>>
    fun getTasksByCategoryAndCompletion(category: Category, isCompleted: Boolean): Flow<List<TodoTask>>
    fun getTasksByPriority(priority: Priority): Flow<List<TodoTask>>
    suspend fun getTaskById(taskId: String): TodoTask?
    suspend fun insertTask(task: TodoTask)
    suspend fun updateTask(task: TodoTask)
    suspend fun deleteTask(taskId: String)
    suspend fun toggleTaskCompletion(taskId: String)
    fun searchTasks(query: String): Flow<List<TodoTask>>
    fun searchTasksWithFilters(
        query: String = "",
        categories: List<Category> = emptyList(),
        priorities: List<Priority> = emptyList(),
        isCompleted: Boolean? = null
    ): Flow<List<TodoTask>>
    suspend fun getTotalTaskCount(): Int
    suspend fun getCompletedTaskCount(): Int
    suspend fun getTaskCountByCategory(category: Category): Int
    
    // Statistics methods
    suspend fun getTaskStatistics(): TaskStatistics
    suspend fun getCompletionRate(): Float
    suspend fun getCurrentStreak(): Int
    suspend fun getCategoryBreakdown(): Map<Category, Int>
    suspend fun getWeeklyProgress(): List<DailyProgress>
    suspend fun getDailyProgress(date: LocalDate): DailyProgress
}