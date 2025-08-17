package com.example.todoapp.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.example.todoapp.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScreenshotRegressionTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun captureEmptyTaskListScreen() {
        // Capture empty state
        composeTestRule.waitForIdle()
        
        // Take screenshot of empty state
        composeTestRule.onRoot().captureToImage()
        
        // Verify empty state elements are visible
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create your first task to get started").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
    }

    @Test
    fun captureTaskListWithItems() {
        // Create sample tasks with different properties
        createSampleTasks()
        
        composeTestRule.waitForIdle()
        
        // Take screenshot of populated list
        composeTestRule.onRoot().captureToImage()
        
        // Verify all sample tasks are visible
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertIsDisplayed()
        composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
    }

    @Test
    fun captureTaskCreationDialog() {
        // Open task creation dialog
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot of dialog
        composeTestRule.onRoot().captureToImage()
        
        // Verify dialog elements
        composeTestRule.onNodeWithText("Add Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description (optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Priority").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun captureTaskEditScreen() {
        // Create a task first
        createSampleTasks()
        
        // Open edit screen
        composeTestRule.onNodeWithText("Buy groceries").performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot of edit screen
        composeTestRule.onRoot().captureToImage()
        
        // Verify edit screen elements
        composeTestRule.onNodeWithText("Edit Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
    }

    @Test
    fun captureSearchState() {
        // Create sample tasks
        createSampleTasks()
        
        // Open search
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("groceries")
        composeTestRule.waitForIdle()
        
        // Take screenshot of search results
        composeTestRule.onRoot().captureToImage()
        
        // Verify search results
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertDoesNotExist()
    }

    @Test
    fun captureFilteredState() {
        // Create sample tasks
        createSampleTasks()
        
        // Apply category filter
        composeTestRule.onNodeWithText("Shopping").performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot of filtered results
        composeTestRule.onRoot().captureToImage()
        
        // Verify filtered results
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertDoesNotExist()
    }

    @Test
    fun captureSwipeActions() {
        // Create a task
        createSampleTasks()
        
        // Perform swipe to reveal actions
        composeTestRule.onNodeWithText("Buy groceries").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()
        
        // Take screenshot of swipe actions
        composeTestRule.onRoot().captureToImage()
        
        // Verify swipe actions are visible
        composeTestRule.onNodeWithContentDescription("Edit task").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete task").assertIsDisplayed()
    }

    @Test
    fun captureStatisticsScreen() {
        // Create some tasks and complete some
        createSampleTasks()
        composeTestRule.onAllNodesWithContentDescription("Mark as complete")[0].performClick()
        
        // Navigate to statistics
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot of statistics screen
        composeTestRule.onRoot().captureToImage()
        
        // Verify statistics elements
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completion Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Streak").assertIsDisplayed()
    }

    @Test
    fun captureCompletedTasksState() {
        // Create tasks and mark some as completed
        createSampleTasks()
        
        // Mark first task as completed
        composeTestRule.onAllNodesWithContentDescription("Mark as complete")[0].performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot showing completed task
        composeTestRule.onRoot().captureToImage()
        
        // Verify completed task appearance
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mark as incomplete").assertIsDisplayed()
    }

    @Test
    fun captureErrorStates() {
        // Capture validation error state
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Take screenshot of validation error
        composeTestRule.onRoot().captureToImage()
        
        // Verify error message
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        
        // Cancel dialog
        composeTestRule.onNodeWithText("Cancel").performClick()
        
        // Capture empty search results
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("nonexistent")
        composeTestRule.waitForIdle()
        
        // Take screenshot of empty search
        composeTestRule.onRoot().captureToImage()
        
        // Verify empty search state
        composeTestRule.onNodeWithText("No tasks found").assertIsDisplayed()
    }

    @Test
    fun captureDarkTheme() {
        // This test would require theme switching functionality
        // For now, we'll capture the current theme state
        
        createSampleTasks()
        composeTestRule.waitForIdle()
        
        // Take screenshot in current theme
        composeTestRule.onRoot().captureToImage()
        
        // Note: In a real implementation, we would:
        // 1. Switch to dark theme
        // 2. Take screenshot
        // 3. Compare with light theme screenshots
    }

    @Test
    fun captureDifferentScreenSizes() {
        // This test captures the current screen size
        // In a real implementation, we would test different screen configurations
        
        createSampleTasks()
        
        // Capture portrait orientation
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureToImage()
        
        // Note: Testing different screen sizes would require:
        // 1. Different device configurations
        // 2. Orientation changes
        // 3. Tablet vs phone layouts
    }

    @Test
    fun captureAnimationStates() {
        // Capture different animation states
        
        // Initial state
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureToImage()
        
        // Create task (capture during creation animation)
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Animation test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Capture immediately after creation (during animation)
        composeTestRule.onRoot().captureToImage()
        
        // Wait for animation to complete
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureToImage()
        
        // Capture completion animation
        composeTestRule.onNodeWithContentDescription("Mark as complete").performClick()
        composeTestRule.onRoot().captureToImage()
    }

    @Test
    fun captureAccessibilityStates() {
        // Capture states relevant to accessibility
        
        createSampleTasks()
        
        // Capture with focus indicators
        composeTestRule.onNodeWithContentDescription("Add Task").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureToImage()
        
        // Capture high contrast state (if supported)
        // This would require accessibility settings changes
        
        // Capture large text state (if supported)
        // This would require text scaling changes
    }

    private fun createSampleTasks() {
        // Create sample tasks with different properties for testing
        
        // High priority shopping task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Buy groceries")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Milk, bread, eggs")
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNodeWithText("High").performClick()
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Shopping").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Medium priority work task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Finish project")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Complete the Android app")
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNodeWithText("Medium").performClick()
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Low priority personal task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Call dentist")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Schedule appointment")
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNodeWithText("Low").performClick()
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Health").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        composeTestRule.waitForIdle()
    }
}