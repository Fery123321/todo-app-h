package com.example.todoapp.data.repository

import com.example.todoapp.data.local.DatabaseErrorHandler
import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.mapper.toDomainModel
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.DailyProgress
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TaskStatistics
import com.example.todoapp.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val errorHandler: DatabaseErrorHandler
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
        errorHandler.executeWithRetry {
            taskDao.insertTask(task.toEntity())
        }.getOrThrow()
    }
    
    override suspend fun updateTask(task: TodoTask) {
        errorHandler.executeWithRetry {
            taskDao.updateTask(task.toEntity())
        }.getOrThrow()
    }
    
    override suspend fun deleteTask(taskId: String) {
        errorHandler.executeWithRetry {
            taskDao.deleteTaskById(taskId)
        }.getOrThrow()
    }
    
    override suspend fun toggleTaskCompletion(taskId: String) {
        errorHandler.executeWithRetry {
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
        }.getOrThrow()
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
    
    override suspend fun getTaskStatistics(): TaskStatistics {
        val totalTasks = getTotalTaskCount()
        val completedTasks = getCompletedTaskCount()
        val completionRate = getCompletionRate()
        val currentStreak = getCurrentStreak()
        val categoryBreakdown = getCategoryBreakdown()
        val weeklyProgress = getWeeklyProgress()
        
        return TaskStatistics(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionRate = completionRate,
            currentStreak = currentStreak,
            categoryBreakdown = categoryBreakdown,
            weeklyProgress = weeklyProgress
        )
    }
    
    override suspend fun getCompletionRate(): Float {
        val totalTasks = getTotalTaskCount()
        if (totalTasks == 0) return 0f
        
        val completedTasks = getCompletedTaskCount()
        return (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
    }
    
    override suspend fun getCurrentStreak(): Int {
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today
        
        // Check each day backwards from today
        while (true) {
            val dailyProgress = getDailyProgress(currentDate)
            
            // If no tasks were completed on this day, break the streak
            if (dailyProgress.completedTasks == 0) {
                // Exception: if it's today and no tasks were created, don't break streak
                if (currentDate == today && dailyProgress.totalTasks == 0) {
                    currentDate = currentDate.minusDays(1)
                    continue
                }
                break
            }
            
            // If tasks were completed, increment streak
            if (dailyProgress.completedTasks > 0) {
                streak++
            }
            
            currentDate = currentDate.minusDays(1)
            
            // Limit streak calculation to reasonable timeframe (e.g., 365 days)
            if (streak > 365) break
        }
        
        return streak
    }
    
    override suspend fun getCategoryBreakdown(): Map<Category, Int> {
        val breakdown = mutableMapOf<Category, Int>()
        
        Category.values().forEach { category ->
            val count = getTaskCountByCategory(category)
            if (count > 0) {
                breakdown[category] = count
            }
        }
        
        return breakdown
    }
    
    override suspend fun getWeeklyProgress(): List<DailyProgress> {
        val today = LocalDate.now()
        val weeklyProgress = mutableListOf<DailyProgress>()
        
        // Get progress for the last 7 days
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dailyProgress = getDailyProgress(date)
            weeklyProgress.add(dailyProgress)
        }
        
        return weeklyProgress
    }
    
    override suspend fun getDailyProgress(date: LocalDate): DailyProgress {
        val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfDay = date.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        val completedTasks = taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay)
        val totalTasks = taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay)
        
        return DailyProgress(
            date = date,
            completedTasks = completedTasks,
            totalTasks = totalTasks
        )
    }
}