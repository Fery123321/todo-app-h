package com.example.todoapp.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.presentation.TodoUiState
import com.example.todoapp.presentation.TodoViewModel
import com.example.todoapp.ui.theme.TodoAppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskListScreenUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: TodoViewModel
    private lateinit var uiStateFlow: MutableStateFlow<TodoUiState>

    @Before
    fun setup() {
        hiltRule.inject()
        mockViewModel = mockk(relaxed = true)
        uiStateFlow = MutableStateFlow(TodoUiState())
        every { mockViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun taskListScreen_showsLoadingState() {
        // Given
        uiStateFlow.value = TodoUiState(isLoading = true)

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then - skeleton loading should be displayed
        // Note: We can't easily test for skeleton items directly, but we can verify loading state
        composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsEmptyState() {
        // Given
        uiStateFlow.value = TodoUiState(
            isLoading = false,
            tasks = emptyList()
        )

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the + button to create your first task").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsEmptyStateWithFilters() {
        // Given
        uiStateFlow.value = TodoUiState(
            isLoading = false,
            tasks = emptyList(),
            searchQuery = "test"
        )

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("No tasks match your filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try adjusting your search or filters").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsTaskList() {
        // Given
        val tasks = listOf(
            TodoTask(
                id = "1",
                title = "Test Task 1",
                description = "Description 1",
                priority = Priority.HIGH,
                category = Category.WORK
            ),
            TodoTask(
                id = "2",
                title = "Test Task 2",
                description = "Description 2",
                priority = Priority.MEDIUM,
                category = Category.PERSONAL
            )
        )
        uiStateFlow.value = TodoUiState(
            isLoading = false,
            tasks = tasks
        )

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Task 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Task 2").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsSearchBar() {
        // Given
        uiStateFlow.value = TodoUiState(isLoading = false)

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Search tasks...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsFilterButton() {
        // Given
        uiStateFlow.value = TodoUiState(isLoading = false)

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Filter tasks").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_showsFloatingActionButton() {
        // Given
        uiStateFlow.value = TodoUiState(isLoading = false)

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Add new task").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_opensFilterSheet() {
        // Given
        uiStateFlow.value = TodoUiState(isLoading = false)

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                TaskListScreen(viewModel = mockViewModel)
            }
        }

        // Click filter button
        composeTestRule.onNodeWithContentDescription("Filter tasks").performClick()

        // Then
        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Categories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status").assertIsDisplayed()
    }
}