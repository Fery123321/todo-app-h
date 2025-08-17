package com.example.todoapp.data.repository

import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class TodoRepositoryImplTest {

    private lateinit var taskDao: TaskDao
    private lateinit var repository: TodoRepositoryImpl

    private val sampleTask = TodoTask(
        id = "1",
        title = "Test Task",
        description = "Test Description",
        isCompleted = false,
        priority = Priority.HIGH,
        category = Category.WORK,
        dueDate = LocalDateTime.now().plusDays(1),
        createdAt = LocalDateTime.now(),
        completedAt = null
    )

    private val sampleTaskEntity = TaskEntity(
        id = "1",
        title = "Test Task",
        description = "Test Description",
        isCompleted = false,
        priority = "HIGH",
        category = "WORK",
        dueDate = LocalDateTime.now().plusDays(1),
        createdAt = LocalDateTime.now(),
        completedAt = null
    )

    @Before
    fun setup() {
        taskDao = mockk()
        repository = TodoRepositoryImpl(taskDao)
    }

    @Test
    fun `getAllTasks returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(sampleTaskEntity)
        every { taskDao.getAllTasks() } returns flowOf(entities)

        // When
        val result = repository.getAllTasks().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleTask.id, result[0].id)
        assertEquals(sampleTask.title, result[0].title)
        assertEquals(sampleTask.priority, result[0].priority)
        assertEquals(sampleTask.category, result[0].category)
    }

    @Test
    fun `getTasksByCategory returns filtered tasks`() = runTest {
        // Given
        val entities = listOf(sampleTaskEntity)
        every { taskDao.getTasksByCategory("WORK") } returns flowOf(entities)

        // When
        val result = repository.getTasksByCategory(Category.WORK).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(Category.WORK, result[0].category)
    }

    @Test
    fun `getTasksByCompletion returns filtered tasks`() = runTest {
        // Given
        val entities = listOf(sampleTaskEntity)
        every { taskDao.getTasksByCompletion(false) } returns flowOf(entities)

        // When
        val result = repository.getTasksByCompletion(false).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(false, result[0].isCompleted)
    }

    @Test
    fun `getTaskById returns mapped domain model when task exists`() = runTest {
        // Given
        coEvery { taskDao.getTaskById("1") } returns sampleTaskEntity

        // When
        val result = repository.getTaskById("1")

        // Then
        assertEquals(sampleTask.id, result?.id)
        assertEquals(sampleTask.title, result?.title)
    }

    @Test
    fun `getTaskById returns null when task does not exist`() = runTest {
        // Given
        coEvery { taskDao.getTaskById("nonexistent") } returns null

        // When
        val result = repository.getTaskById("nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `insertTask calls dao with mapped entity`() = runTest {
        // Given
        coEvery { taskDao.insertTask(any()) } returns Unit

        // When
        repository.insertTask(sampleTask)

        // Then
        coVerify { taskDao.insertTask(any()) }
    }

    @Test
    fun `updateTask calls dao with mapped entity`() = runTest {
        // Given
        coEvery { taskDao.updateTask(any()) } returns Unit

        // When
        repository.updateTask(sampleTask)

        // Then
        coVerify { taskDao.updateTask(any()) }
    }

    @Test
    fun `deleteTask calls dao with task id`() = runTest {
        // Given
        coEvery { taskDao.deleteTaskById("1") } returns Unit

        // When
        repository.deleteTask("1")

        // Then
        coVerify { taskDao.deleteTaskById("1") }
    }

    @Test
    fun `toggleTaskCompletion updates task completion status`() = runTest {
        // Given
        val incompleteTask = sampleTaskEntity.copy(isCompleted = false)
        coEvery { taskDao.getTaskById("1") } returns incompleteTask
        coEvery { taskDao.updateTaskCompletion(any(), any(), any()) } returns Unit

        // When
        repository.toggleTaskCompletion("1")

        // Then
        coVerify { taskDao.updateTaskCompletion("1", true, any()) }
    }

    @Test
    fun `searchTasks returns filtered results`() = runTest {
        // Given
        val entities = listOf(sampleTaskEntity)
        every { taskDao.searchTasks("Test") } returns flowOf(entities)

        // When
        val result = repository.searchTasks("Test").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Test Task", result[0].title)
    }

    @Test
    fun `searchTasksWithFilters applies all filters correctly`() = runTest {
        // Given
        val workTask = sampleTask.copy(id = "1", category = Category.WORK, priority = Priority.HIGH, isCompleted = false)
        val personalTask = sampleTask.copy(id = "2", category = Category.PERSONAL, priority = Priority.LOW, isCompleted = true)
        val tasks = listOf(workTask, personalTask)
        
        every { taskDao.getAllTasks() } returns flowOf(tasks.map { 
            TaskEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                isCompleted = it.isCompleted,
                priority = it.priority.name,
                category = it.category.name,
                dueDate = it.dueDate,
                createdAt = it.createdAt,
                completedAt = it.completedAt
            )
        })

        // When - filter by category and completion status
        val result = repository.searchTasksWithFilters(
            query = "",
            categories = listOf(Category.WORK),
            priorities = emptyList(),
            isCompleted = false
        ).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(Category.WORK, result[0].category)
        assertEquals(false, result[0].isCompleted)
    }

    @Test
    fun `searchTasksWithFilters applies query filter correctly`() = runTest {
        // Given
        val task1 = sampleTask.copy(id = "1", title = "Important Task")
        val task2 = sampleTask.copy(id = "2", title = "Regular Task")
        val tasks = listOf(task1, task2)
        
        every { taskDao.getAllTasks() } returns flowOf(tasks.map { 
            TaskEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                isCompleted = it.isCompleted,
                priority = it.priority.name,
                category = it.category.name,
                dueDate = it.dueDate,
                createdAt = it.createdAt,
                completedAt = it.completedAt
            )
        })

        // When
        val result = repository.searchTasksWithFilters(
            query = "Important",
            categories = emptyList(),
            priorities = emptyList(),
            isCompleted = null
        ).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Important Task", result[0].title)
    }

    @Test
    fun `getTotalTaskCount returns count from dao`() = runTest {
        // Given
        coEvery { taskDao.getTotalTaskCount() } returns 5

        // When
        val result = repository.getTotalTaskCount()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `getCompletedTaskCount returns count from dao`() = runTest {
        // Given
        coEvery { taskDao.getCompletedTaskCount() } returns 3

        // When
        val result = repository.getCompletedTaskCount()

        // Then
        assertEquals(3, result)
    }

    @Test
    fun `getTaskCountByCategory returns count from dao`() = runTest {
        // Given
        coEvery { taskDao.getTaskCountByCategory("WORK") } returns 2

        // When
        val result = repository.getTaskCountByCategory(Category.WORK)

        // Then
        assertEquals(2, result)
    }
}