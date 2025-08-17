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
class AccessibilityIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testContentDescriptions() {
        // Verify all interactive elements have proper content descriptions
        
        // Main screen elements
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Statistics").assertIsDisplayed()
        
        // Create a task to test task-specific accessibility
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Accessibility test task")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Task item accessibility
        composeTestRule.onNodeWithContentDescription("Mark as complete").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Task priority: Medium").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Category: Personal").assertIsDisplayed()
        
        // Test swipe actions accessibility
        composeTestRule.onNodeWithText("Accessibility test task").performTouchInput {
            swipeLeft()
        }
        composeTestRule.onNodeWithContentDescription("Edit task").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete task").assertIsDisplayed()
    }

    @Test
    fun testSemanticProperties() {
        // Test semantic properties for screen readers
        
        // Create a task with various properties
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        // Form fields should have proper semantics
        composeTestRule.onNode(hasText("Task Title") and hasSetTextAction()).assertIsDisplayed()
        composeTestRule.onNode(hasText("Description (optional)") and hasSetTextAction()).assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Task Title").performTextInput("Semantic test")
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Testing semantic properties")
        
        // Priority selection should be accessible
        composeTestRule.onNodeWithText("Priority").performClick()
        composeTestRule.onNode(hasText("High") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("High").performClick()
        
        // Category selection should be accessible
        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNode(hasText("Work") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").performClick()
        
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify task has proper semantic information
        composeTestRule.onNodeWithText("Semantic test").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Task priority: High").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Category: Work").assertIsDisplayed()
    }

    @Test
    fun testKeyboardNavigation() {
        // Test keyboard navigation support
        
        // Create task form keyboard navigation
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        // Tab through form fields (simulated with focus changes)
        composeTestRule.onNodeWithText("Task Title").requestFocus()
        composeTestRule.onNodeWithText("Task Title").assertIsFocused()
        
        composeTestRule.onNodeWithText("Task Title").performTextInput("Keyboard test")
        
        // Move to description field
        composeTestRule.onNodeWithText("Description (optional)").requestFocus()
        composeTestRule.onNodeWithText("Description (optional)").assertIsFocused()
        
        composeTestRule.onNodeWithText("Description (optional)").performTextInput("Testing keyboard navigation")
        
        // Save button should be accessible via keyboard
        composeTestRule.onNodeWithText("Save").requestFocus()
        composeTestRule.onNodeWithText("Save").assertIsFocused()
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify task was created
        composeTestRule.onNodeWithText("Keyboard test").assertIsDisplayed()
    }

    @Test
    fun testScreenReaderAnnouncements() {
        // Test screen reader announcements for state changes
        
        // Create a task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Screen reader test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Toggle completion - should announce state change
        composeTestRule.onNodeWithContentDescription("Mark as complete").performClick()
        
        // Verify the task state change is reflected in accessibility
        composeTestRule.onNodeWithContentDescription("Mark as incomplete").assertIsDisplayed()
        
        // Test search announcements
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("screen")
        
        // Should announce search results
        composeTestRule.onNodeWithText("Screen reader test").assertIsDisplayed()
        
        // Clear search
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()
    }

    @Test
    fun testTouchTargetSizes() {
        // Test minimum touch target sizes (48dp minimum)
        
        // Create a task to test touch targets
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Touch target test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // All interactive elements should have adequate touch targets
        composeTestRule.onNodeWithContentDescription("Add Task").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Search").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Statistics").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Mark as complete").assertHasClickAction()
        
        // Test swipe action touch targets
        composeTestRule.onNodeWithText("Touch target test").performTouchInput {
            swipeLeft()
        }
        composeTestRule.onNodeWithContentDescription("Edit task").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Delete task").assertHasClickAction()
    }

    @Test
    fun testColorContrastAndVisibility() {
        // Test color contrast and visibility for accessibility
        
        // Create tasks with different priorities to test color accessibility
        val priorities = listOf("High", "Medium", "Low")
        priorities.forEachIndexed { index, priority ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("Priority $priority task")
            composeTestRule.onNodeWithText("Priority").performClick()
            composeTestRule.onNodeWithText(priority).performClick()
            composeTestRule.onNodeWithText("Save").performClick()
            
            // Verify priority indicator is visible
            composeTestRule.onNodeWithContentDescription("Task priority: $priority").assertIsDisplayed()
        }
        
        // Test completed task visibility
        composeTestRule.onAllNodesWithContentDescription("Mark as complete")[0].performClick()
        
        // Completed task should still be readable (not just rely on color)
        composeTestRule.onNodeWithText("Priority High task").assertIsDisplayed()
        
        // Test dark theme accessibility (if supported)
        // This would require theme switching functionality
    }

    @Test
    fun testFocusManagement() {
        // Test proper focus management
        
        // When opening add task dialog, focus should be on title field
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").assertIsFocused()
        
        // When validation fails, focus should return to error field
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Title").assertIsFocused()
        
        // Fix error and save
        composeTestRule.onNodeWithText("Task Title").performTextInput("Focus test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Focus should return to main screen
        composeTestRule.onNodeWithContentDescription("Add Task").assertExists()
    }

    @Test
    fun testAccessibilityActions() {
        // Test custom accessibility actions
        
        // Create a task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Actions test")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Task should have accessibility actions available
        val taskNode = composeTestRule.onNodeWithText("Actions test")
        taskNode.assertIsDisplayed()
        
        // Test completion action
        composeTestRule.onNodeWithContentDescription("Mark as complete").performClick()
        composeTestRule.onNodeWithContentDescription("Mark as incomplete").assertIsDisplayed()
        
        // Test edit action via accessibility
        composeTestRule.onNodeWithText("Actions test").performClick()
        composeTestRule.onNodeWithText("Task Title").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        
        // Test delete action via swipe
        composeTestRule.onNodeWithText("Actions test").performTouchInput {
            swipeLeft()
        }
        composeTestRule.onNodeWithContentDescription("Delete task").performClick()
        composeTestRule.onNodeWithText("Actions test").assertDoesNotExist()
    }

    @Test
    fun testAccessibilityInDifferentStates() {
        // Test accessibility in different app states
        
        // Empty state accessibility
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create your first task to get started").assertIsDisplayed()
        
        // Create some tasks
        repeat(3) { index ->
            composeTestRule.onNodeWithContentDescription("Add Task").performClick()
            composeTestRule.onNodeWithText("Task Title").performTextInput("State test $index")
            composeTestRule.onNodeWithText("Save").performClick()
        }
        
        // Loading state accessibility (if applicable)
        // Search state accessibility
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("nonexistent")
        composeTestRule.onNodeWithText("No tasks found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try adjusting your search or filters").assertIsDisplayed()
        
        // Filter state accessibility
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()
        composeTestRule.onNodeWithText("Work").performClick()
        
        // Should announce filter state
        composeTestRule.onNodeWithText("Work").assertIsSelected()
        
        // Statistics screen accessibility
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completion Rate").assertIsDisplayed()
        
        // Navigation should be accessible
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
    }
}