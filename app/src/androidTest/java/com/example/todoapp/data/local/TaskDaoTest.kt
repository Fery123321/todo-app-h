package com.example.todoapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TodoDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TodoDatabase::class.java
        ).allowMainThreadQueries().build()
        
        taskDao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTask_and_getTaskById() = runTest {
        // Given
        val task = createSampleTask()

        // When
        taskDao.insertTask(task)
        val retrievedTask = taskDao.getTaskById(task.id)

        // Then
        assertNotNull(retrievedTask)
        assertEquals(task.id, retrievedTask!!.id)
        assertEquals(task.title, retrievedTask.title)
        assertEquals(task.description, retrievedTask.description)
    }

    @Test
    fun getAllTasks_returnsTasksInDescendingOrder() = runTest {
        // Given
        val task1 = createSampleTask(id = "1", title = "Task 1")
        val task2 = createSampleTask(id = "2", title = "Task 2", 
            createdAt = LocalDateTime.now().plusMinutes(1))

        // When
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        val tasks = taskDao.getAllTasks().first()

        // Then
        assertEquals(2, tasks.size)
        assertEquals("Task 2", tasks[0].title) // Most recent first
        assertEquals("Task 1", tasks[1].title)
    }

    @Test
    fun getTasksByCategory_filtersCorrectly() = runTest {
        // Given
        val workTask = createSampleTask(id = "1", category = "WORK")
        val personalTask = createSampleTask(id = "2", category = "PERSONAL")

        // When
        taskDao.insertTask(workTask)
        taskDao.insertTask(personalTask)
        val workTasks = taskDao.getTasksByCategory("WORK").first()

        // Then
        assertEquals(1, workTasks.size)
        assertEquals("WORK", workTasks[0].category)
    }

    @Test
    fun updateTaskCompletion_updatesStatus() = runTest {
        // Given
        val task = createSampleTask(isCompleted = false)
        taskDao.insertTask(task)
        val completedAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // When
        taskDao.updateTaskCompletion(task.id, true, completedAt)
        val updatedTask = taskDao.getTaskById(task.id)

        // Then
        assertNotNull(updatedTask)
        assertTrue(updatedTask!!.isCompleted)
        assertNotNull(updatedTask.completedAt)
    }

    @Test
    fun searchTasks_findsMatchingTasks() = runTest {
        // Given
        val task1 = createSampleTask(id = "1", title = "Important meeting", description = "Team sync")
        val task2 = createSampleTask(id = "2", title = "Buy groceries", description = "Important items")
        val task3 = createSampleTask(id = "3", title = "Read book", description = "Fiction novel")

        // When
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)
        val searchResults = taskDao.searchTasks("important").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.title.contains("Important", ignoreCase = true) })
        assertTrue(searchResults.any { it.description.contains("Important", ignoreCase = true) })
    }

    private fun createSampleTask(
        id: String = "test-id",
        title: String = "Test Task",
        description: String = "Test Description",
        isCompleted: Boolean = false,
        priority: String = "MEDIUM",
        category: String = "PERSONAL",
        dueDate: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
        completedAt: LocalDateTime? = null
    ) = TaskEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority,
        category = category,
        dueDate = dueDate,
        createdAt = createdAt,
        completedAt = completedAt
    )
}