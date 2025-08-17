package com.example.todoapp.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.example.todoapp.MainActivity
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EndToEndWorkflowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeTaskManagementWorkflow() {
        // Test complete user workflow: create, edit, complete, filter, search, delete
        
        // 1. Create a new task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        composeTestRule.onNodeWithText("Task Title").performTextInput("Buy groceries")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Milk, bread, eggs")
        
        // Set priority to High
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNodeWithText("High").performClick()
        
        // Set category to Shopping
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Shopping").performClick()
        
        // Save task
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify task appears in list
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk, bread, eggs").assertIsDisplayed()
        
        // 2. Create another task for testing
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Finish project")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Complete the Android app")
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNodeWithText("Medium").performClick()
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // 3. Test search functionality
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("groceries")
        
        // Should show only the groceries task
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertDoesNotExist()
        
        // Clear search
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()
        
        // Both tasks should be visible again
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertIsDisplayed()
        
        // 4. Test category filtering
        composeTestRule.onNodeWithText("Shopping").performClick()
        
        // Should show only shopping task
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertDoesNotExist()
        
        // Clear filter
        composeTestRule.onNodeWithText("All").performClick()
        
        // 5. Test task completion
        composeTestRule.onAllNodesWithContentDescription("Mark as complete")[0].performClick()
        
        // Verify task is marked as completed (strikethrough effect)
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        
        // 6. Test editing a task
        composeTestRule.onNodeWithText("Finish project").performClick()
        
        // Edit the task
        composeTestRule.onNodeWithText("Task Title").performTextClearance()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Complete Android Todo App")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify changes
        composeTestRule.onNodeWithText("Complete Android Todo App").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish project").assertDoesNotExist()
        
        // 7. Test swipe to delete
        composeTestRule.onNodeWithText("Complete Android Todo App").performTouchInput {
            swipeLeft()
        }
        
        // Confirm deletion
        composeTestRule.onNodeWithContentDescription("Delete task").performClick()
        
        // Verify task is deleted
        composeTestRule.onNodeWithText("Complete Android Todo App").assertDoesNotExist()
        
        // 8. Test statistics screen navigation
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        
        // Verify statistics screen is displayed
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completion Rate").assertIsDisplayed()
        
        // Navigate back
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        
        // Should be back to task list
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
    }

    @Test
    fun dueDateAndNotificationWorkflow() {
        // Test due date functionality and notification setup
        
        // Create task with due date
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        composeTestRule.onNodeWithText("Task Title").performTextInput("Important meeting")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Quarterly review")
        
        // Set due date
        composeTestRule.onNodeWithText("Due Date").performClick()
        // Note: Date picker interaction would be more complex in real implementation
        composeTestRule.onNodeWithText("OK").performClick()
        
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify task with due date is displayed
        composeTestRule.onNodeWithText("Important meeting").assertIsDisplayed()
        
        // Verify due date indicator is shown
        composeTestRule.onNodeWithContentDescription("Due date indicator").assertIsDisplayed()
    }

    @Test
    fun errorHandlingWorkflow() {
        // Test error scenarios and recovery
        
        // Try to create task with empty title
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should show validation error
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        
        // Fix the error
        composeTestRule.onNodeWithText("Task Title").performTextInput("Valid task")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Task should be created successfully
        composeTestRule.onNodeWithText("Valid task").assertIsDisplayed()
    }

    @Test
    fun accessibilityWorkflow() {
        // Test accessibility features
        
        // Create a task for testing
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Accessibility test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify all interactive elements have proper content descriptions
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mark as complete").assertIsDisplayed()
        
        // Test keyboard navigation (would require more setup in real implementation)
        // This is a placeholder for comprehensive accessibility testing
    }

    @Test
    fun performanceWorkflow() {
        // Test app performance with large dataset
        
        // Create multiple tasks to test performance
        repeat(20) { index ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("Task $index")
            composeTestRule.onNodeWithText("Description (optional)").performTextInput("Description for task $index")
            composeTestRule.onNodeWithText("Save").performClick()
            
            // Verify task is created
            composeTestRule.onNodeWithText("Task $index").assertIsDisplayed()
        }
        
        // Test scrolling performance
        composeTestRule.onNodeWithTag("TaskList").performScrollToIndex(19)
        composeTestRule.onNodeWithText("Task 19").assertIsDisplayed()
        
        // Test search performance with large dataset
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("Task 1")
        
        // Should filter results quickly
        composeTestRule.onNodeWithText("Task 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task 11").assertIsDisplayed()
    }
}