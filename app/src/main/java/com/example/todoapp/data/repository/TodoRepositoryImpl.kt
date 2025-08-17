package com.example.todoapp.data.repository

import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.mapper.toDomainModel
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TodoRepository {
    
    override fun getAllTasks(): Flow<List<TodoTask>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getTasksByCategory(category: Category): Flow<List<TodoTask>> {
        return taskDao.getTasksByCategory(category.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TodoTask>> {
        return taskDao.getTasksByCompletion(isCompleted).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getTasksByCategoryAndCompletion(category: Category, isCompleted: Boolean): Flow<List<TodoTask>> {
        return taskDao.getTasksByCategoryAndCompletion(category.name, isCompleted).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getTasksByPriority(priority: Priority): Flow<List<TodoTask>> {
        return taskDao.getTasksByPriority(priority.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getTaskById(taskId: String): TodoTask? {
        return taskDao.getTaskById(taskId)?.toDomainModel()
    }
    
    override suspend fun insertTask(task: TodoTask) {
        taskDao.insertTask(task.toEntity())
    }
    
    override suspend fun updateTask(task: TodoTask) {
        taskDao.updateTask(task.toEntity())
    }
    
    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }
    
    override suspend fun toggleTaskCompletion(taskId: String) {
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            val newCompletionStatus = !task.isCompleted
            val completedAt = if (newCompletionStatus) {
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            } else {
                null
            }
            taskDao.updateTaskCompletion(taskId, newCompletionStatus, completedAt)
        }
    }
    
    override fun searchTasks(query: String): Flow<List<TodoTask>> {
        return taskDao.searchTasks(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun searchTasksWithFilters(
        query: String,
        categories: List<Category>,
        priorities: List<Priority>,
        isCompleted: Boolean?
    ): Flow<List<TodoTask>> {
        return getAllTasks().map { tasks ->
            tasks.filter { task ->
                // Apply search query filter
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
                }
                
                // Apply category filter
                val matchesCategory = if (categories.isEmpty()) {
                    true
                } else {
                    categories.contains(task.category)
                }
                
                // Apply priority filter
                val matchesPriority = if (priorities.isEmpty()) {
                    true
                } else {
                    priorities.contains(task.priority)
                }
                
                // Apply completion status filter
                val matchesCompletion = isCompleted?.let { completed ->
                    task.isCompleted == completed
                } ?: true
                
                matchesQuery && matchesCategory && matchesPriority && matchesCompletion
            }
        }
    }
    
    override suspend fun getTotalTaskCount(): Int {
        return taskDao.getTotalTaskCount()
    }
    
    override suspend fun getCompletedTaskCount(): Int {
        return taskDao.getCompletedTaskCount()
    }
    
    override suspend fun getTaskCountByCategory(category: Category): Int {
        return taskDao.getTaskCountByCategory(category.name)
    }
}