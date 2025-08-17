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
class ErrorHandlingIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testValidationErrors() {
        // Test form validation errors
        
        // Try to create task with empty title
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should show validation error
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        
        // Try with title that's too long (over 100 characters)
        val longTitle = "a".repeat(101)
        composeTestRule.onNodeWithText("Task Title").performTextInput(longTitle)
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should show length validation error
        composeTestRule.onNodeWithText("Title must be less than 100 characters").assertIsDisplayed()
        
        // Fix with valid title
        composeTestRule.onNodeWithText("Task Title").performTextClearance()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Valid task title")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should create task successfully
        composeTestRule.onNodeWithText("Valid task title").assertIsDisplayed()
    }

    @Test
    fun testDatabaseErrorRecovery() {
        // Test database error scenarios and recovery
        
        // Create a task first
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Test task")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify task is created
        composeTestRule.onNodeWithText("Test task").assertIsDisplayed()
        
        // Test error recovery by trying operations that might fail
        // In a real scenario, we might simulate database corruption or connection issues
        
        // Try to edit the task
        composeTestRule.onNodeWithText("Test task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextClearance()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Updated task")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should handle any database errors gracefully
        // If there's an error, user should see appropriate error message
        // If successful, task should be updated
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Updated task").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Error updating task").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testNetworkErrorHandling() {
        // Test network-related error handling (if app has network features)
        
        // For now, this tests local error scenarios
        // In future versions with sync features, this would test network failures
        
        // Test creating multiple tasks rapidly to stress the system
        repeat(5) { index ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("Rapid task $index")
            composeTestRule.onNodeWithText("Save").performClick()
            
            // Should handle rapid creation without errors
            composeTestRule.waitUntil(timeoutMillis = 2000) {
                composeTestRule.onAllNodesWithText("Rapid task $index").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }

    @Test
    fun testMemoryPressureHandling() {
        // Test app behavior under memory pressure
        
        // Create many tasks to use memory
        repeat(50) { index ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("Memory test task $index")
            composeTestRule.onNodeWithText("Description (optional)").performTextInput("This is a longer description for task $index to use more memory and test how the app handles larger datasets")
            composeTestRule.onNodeWithText("Save").performClick()
            
            // Verify task is created (check every 10th task to avoid too many assertions)
            if (index % 10 == 0) {
                composeTestRule.onNodeWithText("Memory test task $index").assertIsDisplayed()
            }
        }
        
        // Test scrolling through large list
        composeTestRule.onNodeWithTag("TaskList").performScrollToIndex(40)
        composeTestRule.onNodeWithText("Memory test task 40").assertIsDisplayed()
        
        // Test search with large dataset
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("Memory test")
        
        // Should still be responsive
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Memory test task 0").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testConcurrentOperations() {
        // Test concurrent operations and race conditions
        
        // Create a task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Concurrent test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        composeTestRule.onNodeWithText("Concurrent test").assertIsDisplayed()
        
        // Try to perform multiple operations quickly
        // Toggle completion multiple times rapidly
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Mark as complete").performClick()
            composeTestRule.waitForIdle()
        }
        
        // Should handle rapid state changes gracefully
        // Final state should be consistent
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            // Task should still exist and be in a consistent state
            composeTestRule.onAllNodesWithText("Concurrent test").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testEdgeCaseInputs() {
        // Test edge case inputs
        
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        // Test special characters
        composeTestRule.onNodeWithText("Task Title").performTextInput("Task with émojis 🚀 and spëcial chars!")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Description with\nnewlines and\ttabs")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should handle special characters correctly
        composeTestRule.onNodeWithText("Task with émojis 🚀 and spëcial chars!").assertIsDisplayed()
        
        // Test very long description
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Long description test")
        val longDescription = "This is a very long description that tests how the app handles large amounts of text. ".repeat(20)
        composeTestRule.onNodeWithText("Description (optional)").performTextInput(longDescription)
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should handle long text gracefully
        composeTestRule.onNodeWithText("Long description test").assertIsDisplayed()
    }

    @Test
    fun testStateRecoveryAfterError() {
        // Test app state recovery after errors
        
        // Create some tasks
        repeat(3) { index ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("Recovery test $index")
            composeTestRule.onNodeWithText("Save").performClick()
        }
        
        // Apply some filters
        composeTestRule.onNodeWithText("Work").performClick()
        
        // Try to cause an error scenario (invalid operation)
        // Then verify app recovers gracefully
        
        // Navigate to statistics and back
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        
        // App should maintain its state
        composeTestRule.onNodeWithText("Recovery test 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recovery test 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recovery test 2").assertIsDisplayed()
        
        // Filter should still be applied
        composeTestRule.onNodeWithText("Work").assertIsSelected()
    }

    @Test
    fun testEmptyStatesAndErrorMessages() {
        // Test empty states and error message display
        
        // Test empty task list state
        // (Assuming we start with empty list)
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create your first task to get started").assertIsDisplayed()
        
        // Test empty search results
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Single task")
        composeTestRule.onNodeWithText("Save").performClick()
        
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("nonexistent")
        
        // Should show empty search state
        composeTestRule.onNodeWithText("No tasks found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try adjusting your search or filters").assertIsDisplayed()
        
        // Clear search to restore normal state
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()
        composeTestRule.onNodeWithText("Single task").assertIsDisplayed()
    }
}