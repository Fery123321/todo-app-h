package com.example.todoapp.data.repository

import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.mapper.toDomainModel
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    
    override fun getTasksByCategory(category: String): Flow<List<TodoTask>> {
        return taskDao.getTasksByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TodoTask>> {
        return taskDao.getTasksByCompletion(isCompleted).map { entities ->
            entities.map { it.toDomainModel() }
        }
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
        // This will be implemented in later tasks
        TODO("Implementation will be added in task 3.1")
    }
    
    override fun searchTasks(query: String): Flow<List<TodoTask>> {
        return taskDao.searchTasks(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}