package com.example.todoapp.presentation

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TaskStatistics
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.notification.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    private lateinit var repository: TodoRepository
    private lateinit var reminderScheduler: TaskReminderScheduler
    private lateinit var viewModel: TodoViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleTasks = listOf(
        TodoTask(
            id = "1",
            title = "Test Task 1",
            description = "Description 1",
            isCompleted = false,
            priority = Priority.HIGH,
            category = Category.WORK
        ),
        TodoTask(
            id = "2",
            title = "Test Task 2",
            description = "Description 2",
            isCompleted = true,
            priority = Priority.LOW,
            category = Category.PERSONAL
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        reminderScheduler = mockk(relaxed = true)
        every { repository.getAllTasks() } returns flowOf(sampleTasks)
        coEvery { repository.getTaskStatistics() } returns com.example.todoapp.domain.model.TaskStatistics()
        viewModel = TodoViewModel(repository, reminderScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        // Given - fresh ViewModel
        every { repository.getAllTasks() } returns flowOf(emptyList())
        coEvery { repository.getTaskStatistics() } returns TaskStatistics()
        val freshViewModel = TodoViewModel(repository, reminderScheduler)
        
        // When - checking initial state before tasks load
        val initialState = freshViewModel.uiState.value
        
        // Then - should have correct default values
        assertEquals("", initialState.searchQuery)
        assertNull(initialState.selectedCategory)
        assertTrue(initialState.showCompletedTasks)
        assertNull(initialState.errorMessage)
        
        // After tasks load
        advanceUntilIdle()
        val loadedState = freshViewModel.uiState.value
        assertFalse(loadedState.isLoading)
        assertTrue(loadedState.tasks.isEmpty())
    }

    @Test
    fun `loadTasks should update UI state with tasks`() = runTest {
        // When - tasks are loaded
        advanceUntilIdle()
        
        // Then - UI state should be updated
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleTasks, state.tasks)
        assertNull(state.errorMessage)
    }

    @Test
    fun `createTask should call repository insertTask`() = runTest {
        // Given
        coEvery { repository.insertTask(any()) } returns Unit
        
        // When
        viewModel.createTask(
            title = "New Task",
            description = "New Description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        advanceUntilIdle()
        
        // Then
        coVerify { repository.insertTask(any()) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `createTask with empty title should show error`() = runTest {
        // When
        viewModel.createTask(title = "")
        advanceUntilIdle()
        
        // Then
        assertEquals("Task title cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { repository.insertTask(any()) }
    }

    @Test
    fun `createTask with blank title should show error`() = runTest {
        // When
        viewModel.createTask(title = "   ")
        advanceUntilIdle()
        
        // Then
        assertEquals("Task title cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { repository.insertTask(any()) }
    }

    @Test
    fun `updateTask should call repository updateTask`() = runTest {
        // Given
        val taskToUpdate = sampleTasks[0].copy(title = "Updated Task")
        coEvery { repository.updateTask(any()) } returns Unit
        
        // When
        viewModel.updateTask(taskToUpdate)
        advanceUntilIdle()
        
        // Then
        coVerify { repository.updateTask(taskToUpdate) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updateTask with empty title should show error`() = runTest {
        // Given
        val taskWithEmptyTitle = sampleTasks[0].copy(title = "")
        
        // When
        viewModel.updateTask(taskWithEmptyTitle)
        advanceUntilIdle()
        
        // Then
        assertEquals("Task title cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { repository.updateTask(any()) }
    }

    @Test
    fun `deleteTask should call repository deleteTask`() = runTest {
        // Given
        val taskId = "1"
        coEvery { repository.deleteTask(taskId) } returns Unit
        
        // When
        viewModel.deleteTask(taskId)
        advanceUntilIdle()
        
        // Then
        coVerify { repository.deleteTask(taskId) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `toggleTaskCompletion should call repository toggleTaskCompletion`() = runTest {
        // Given
        val taskId = "1"
        coEvery { repository.toggleTaskCompletion(taskId) } returns Unit
        
        // When
        viewModel.toggleTaskCompletion(taskId)
        advanceUntilIdle()
        
        // Then
        coVerify { repository.toggleTaskCompletion(taskId) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `repository error should update error message`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { repository.insertTask(any()) } throws Exception(errorMessage)
        
        // When
        viewModel.createTask("Test Task")
        advanceUntilIdle()
        
        // Then
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError should remove error message`() = runTest {
        // Given - error state
        coEvery { repository.insertTask(any()) } throws Exception("Test error")
        viewModel.createTask("Test Task")
        advanceUntilIdle()
        assertEquals("Test error", viewModel.uiState.value.errorMessage)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `createTask should trim title and description`() = runTest {
        // Given
        coEvery { repository.insertTask(any()) } returns Unit
        
        // When
        viewModel.createTask(
            title = "  Test Task  ",
            description = "  Test Description  "
        )
        advanceUntilIdle()
        
        // Then
        coVerify { 
            repository.insertTask(match { task ->
                task.title == "Test Task" && task.description == "Test Description"
            })
        }
    }

    @Test
    fun `createTask with due date should create task with correct due date`() = runTest {
        // Given
        val dueDate = LocalDateTime.now().plusDays(1)
        coEvery { repository.insertTask(any()) } returns Unit
        
        // When
        viewModel.createTask(
            title = "Task with due date",
            dueDate = dueDate
        )
        advanceUntilIdle()
        
        // Then
        coVerify { 
            repository.insertTask(match { task ->
                task.dueDate == dueDate
            })
        }
    }

    // Search and Filter Tests

    @Test
    fun `updateSearchQuery should filter tasks by title`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When
        viewModel.updateSearchQuery("Task 1")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Task 1", state.searchQuery)
        assertEquals(1, state.tasks.size)
        assertEquals("Test Task 1", state.tasks[0].title)
    }

    @Test
    fun `updateSearchQuery should filter tasks by description`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When
        viewModel.updateSearchQuery("Description 2")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Description 2", state.searchQuery)
        assertEquals(1, state.tasks.size)
        assertEquals("Test Task 2", state.tasks[0].title)
    }

    @Test
    fun `updateSearchQuery should be case insensitive`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When
        viewModel.updateSearchQuery("test task")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `clearSearch should reset search query and show all tasks`() = runTest {
        // Given - tasks are loaded and search is applied
        advanceUntilIdle()
        viewModel.updateSearchQuery("Task 1")
        assertEquals(1, viewModel.uiState.value.tasks.size)
        
        // When
        viewModel.clearSearch()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `selectCategory should filter tasks by category`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When
        viewModel.selectCategory(Category.WORK)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(Category.WORK, state.selectedCategory)
        assertEquals(1, state.tasks.size)
        assertEquals(Category.WORK, state.tasks[0].category)
    }

    @Test
    fun `selectCategory with null should show all categories`() = runTest {
        // Given - tasks are loaded and category filter is applied
        advanceUntilIdle()
        viewModel.selectCategory(Category.WORK)
        assertEquals(1, viewModel.uiState.value.tasks.size)
        
        // When
        viewModel.selectCategory(null)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.selectedCategory)
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `toggleShowCompletedTasks should toggle completion filter`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showCompletedTasks)
        assertEquals(2, viewModel.uiState.value.tasks.size)
        
        // When
        viewModel.toggleShowCompletedTasks()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showCompletedTasks)
        assertEquals(1, state.tasks.size)
        assertFalse(state.tasks[0].isCompleted)
    }

    @Test
    fun `setShowCompletedTasks should set completion filter`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When
        viewModel.setShowCompletedTasks(false)
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showCompletedTasks)
        assertEquals(1, state.tasks.size)
        assertFalse(state.tasks[0].isCompleted)
    }

    @Test
    fun `clearAllFilters should reset all filters`() = runTest {
        // Given - tasks are loaded and filters are applied
        advanceUntilIdle()
        viewModel.updateSearchQuery("Task 1")
        viewModel.selectCategory(Category.WORK)
        viewModel.setShowCompletedTasks(false)
        
        // When
        viewModel.clearAllFilters()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertTrue(state.showCompletedTasks)
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `multiple filters should work together`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When - apply search and category filter
        viewModel.updateSearchQuery("Test")
        viewModel.selectCategory(Category.WORK)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.tasks.size)
        assertEquals("Test Task 1", state.tasks[0].title)
        assertEquals(Category.WORK, state.tasks[0].category)
    }

    @Test
    fun `getFilteredTasksCount should return correct count`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When - apply filter
        viewModel.selectCategory(Category.WORK)
        
        // Then
        assertEquals(1, viewModel.getFilteredTasksCount())
    }

    @Test
    fun `getTotalTasksCount should return total count regardless of filters`() = runTest {
        // Given - tasks are loaded
        advanceUntilIdle()
        
        // When - apply filter
        viewModel.selectCategory(Category.WORK)
        
        // Then
        assertEquals(2, viewModel.getTotalTasksCount())
        assertEquals(1, viewModel.getFilteredTasksCount())
    }
}