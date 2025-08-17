package com.example.todoapp.performance

import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.data.repository.TodoRepositoryImpl
import com.example.todoapp.data.local.DatabaseErrorHandler
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

class PerformanceTest {
    
    private lateinit var taskDao: TaskDao
    private lateinit var errorHandler: DatabaseErrorHandler
    private lateinit var repository: TodoRepositoryImpl
    
    @Before
    fun setup() {
        taskDao = mockk()
        errorHandler = mockk()
        repository = TodoRepositoryImpl(taskDao, errorHandler)
        
        // Mock error handler to pass through operations
        coEvery { errorHandler.executeWithRetry<Unit>(any(), any(), any()) } coAnswers {
            Result.success(firstArg<suspend () -> Unit>().invoke())
        }
    }
    
    @Test
    fun `repository should handle large dataset efficiently`() = runTest {
        // Given - Large dataset of 10,000 tasks
        val largeTasks = generateLargeTasks(10000)
        every { taskDao.getAllTasks() } returns flowOf(largeTasks)
        
        // When - Measure time to retrieve all tasks
        val executionTime = measureTimeMillis {
            val result = repository.getAllTasks().first()
            assertEquals(10000, result.size)
        }
        
        // Then - Should complete within reasonable time (< 1 second)
        assertTrue("Large dataset retrieval took ${executionTime}ms", executionTime < 1000)
    }
    
    @Test
    fun `search should be efficient with large dataset`() = runTest {
        // Given - Large dataset with searchable content
        val largeTasks = generateLargeTasks(5000)
        val searchQuery = "test"
        val filteredTasks = largeTasks.filter { 
            it.title.contains(searchQuery, ignoreCase = true) 
        }
        
        every { taskDao.searchTasks(searchQuery) } returns flowOf(filteredTasks)
        
        // When - Measure search time
        val executionTime = measureTimeMillis {
            val result = repository.searchTasks(searchQuery).first()
            assertTrue("Search should return results", result.isNotEmpty())
        }
        
        // Then - Search should be fast (< 500ms)
        assertTrue("Search took ${executionTime}ms", executionTime < 500)
    }
    
    @Test
    fun `filtering should be efficient with large dataset`() = runTest {
        // Given - Large dataset
        val largeTasks = generateLargeTasks(5000)
        every { taskDao.getAllTasks() } returns flowOf(largeTasks)
        
        // When - Measure filtering time
        val executionTime = measureTimeMillis {
            val result = repository.searchTasksWithFilters(
                query = "",
                categories = listOf(Category.WORK),
                priorities = listOf(Priority.HIGH),
                isCompleted = false
            ).first()
            
            // Verify filtering worked
            result.forEach { task ->
                assertEquals(Category.WORK, task.category)
                assertEquals(Priority.HIGH, task.priority)
                assertFalse(task.isCompleted)
            }
        }
        
        // Then - Filtering should be efficient (< 200ms)
        assertTrue("Filtering took ${executionTime}ms", executionTime < 200)
    }
    
    @Test
    fun `statistics calculation should be efficient with large dataset`() = runTest {
        // Given - Large dataset
        val largeTasks = generateLargeTasks(10000)
        
        // Mock DAO methods for statistics
        every { taskDao.getAllTasks() } returns flowOf(largeTasks)
        coEvery { taskDao.getTotalTaskCount() } returns largeTasks.size
        coEvery { taskDao.getCompletedTaskCount() } returns largeTasks.count { it.isCompleted }
        
        Category.values().forEach { category ->
            coEvery { taskDao.getTaskCountByCategory(category.name) } returns 
                largeTasks.count { it.category == category.name }
        }
        
        coEvery { taskDao.getCompletedTaskCountInDateRange(any(), any()) } returns 100
        coEvery { taskDao.getTotalTaskCountInDateRange(any(), any()) } returns 200
        
        // When - Measure statistics calculation time
        val executionTime = measureTimeMillis {
            val stats = repository.getTaskStatistics()
            
            // Verify statistics are calculated
            assertTrue("Total tasks should be positive", stats.totalTasks > 0)
            assertTrue("Completion rate should be valid", stats.completionRate >= 0f)
        }
        
        // Then - Statistics should be calculated efficiently (< 1 second)
        assertTrue("Statistics calculation took ${executionTime}ms", executionTime < 1000)
    }
    
    @Test
    fun `batch operations should be efficient`() = runTest {
        // Given - Batch of tasks to insert
        val batchSize = 1000
        val taskBatch = generateLargeTasks(batchSize)
        
        coEvery { taskDao.insertTasks(any()) } just Runs
        
        // When - Measure batch insert time
        val executionTime = measureTimeMillis {
            // Simulate batch insert by inserting tasks one by one
            taskBatch.forEach { taskEntity ->
                val task = TodoTask(
                    id = taskEntity.id,
                    title = taskEntity.title,
                    description = taskEntity.description,
                    isCompleted = taskEntity.isCompleted,
                    priority = Priority.valueOf(taskEntity.priority),
                    category = Category.valueOf(taskEntity.category),
                    dueDate = taskEntity.dueDate,
                    createdAt = taskEntity.createdAt,
                    completedAt = taskEntity.completedAt
                )
                repository.insertTask(task)
            }
        }
        
        // Then - Batch operations should be efficient (< 2 seconds for 1000 items)
        assertTrue("Batch insert took ${executionTime}ms", executionTime < 2000)
        
        // Verify all inserts were called
        coVerify(exactly = batchSize) { taskDao.insertTask(any()) }
    }
    
    @Test
    fun `memory usage should be reasonable with large dataset`() = runTest {
        // Given - Very large dataset
        val veryLargeTasks = generateLargeTasks(50000)
        every { taskDao.getAllTasks() } returns flowOf(veryLargeTasks)
        
        // When - Process large dataset
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        val result = repository.getAllTasks().first()
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = (finalMemory - initialMemory) / 1024 / 1024 // Convert to MB
        
        // Then - Memory increase should be reasonable (< 100MB for 50k tasks)
        assertTrue("Memory increased by ${memoryIncrease}MB", memoryIncrease < 100)
        assertEquals(50000, result.size)
    }
    
    private fun generateLargeTasks(count: Int): List<TaskEntity> {
        val categories = Category.values()
        val priorities = Priority.values()
        
        return (1..count).map { index ->
            TaskEntity(
                id = "task_$index",
                title = "Test Task $index",
                description = "Description for task $index with some searchable content",
                isCompleted = index % 3 == 0, // Every 3rd task is completed
                priority = priorities[index % priorities.size].name,
                category = categories[index % categories.size].name,
                dueDate = if (index % 5 == 0) LocalDateTime.now().plusDays(index.toLong()) else null,
                createdAt = LocalDateTime.now().minusDays(index.toLong()),
                completedAt = if (index % 3 == 0) LocalDateTime.now() else null
            )
        }
    }
}