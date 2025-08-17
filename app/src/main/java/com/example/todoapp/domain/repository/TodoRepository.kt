package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTasks(): Flow<List<TodoTask>>
    fun getTasksByCategory(category: String): Flow<List<TodoTask>>
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TodoTask>>
    suspend fun insertTask(task: TodoTask)
    suspend fun updateTask(task: TodoTask)
    suspend fun deleteTask(taskId: String)
    suspend fun toggleTaskCompletion(taskId: String)
    fun searchTasks(query: String): Flow<List<TodoTask>>
}