package com.example.todoapp.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeableTaskItemUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeableTaskItem_displaysTaskContent() {
        val task = TodoTask(
            title = "Swipeable Test Task",
            description = "This task can be swiped"
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Swipeable Test Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("This task can be swiped").assertIsDisplayed()
    }

    @Test
    fun swipeableTaskItem_showsActionButtonsAfterSwipe() {
        val task = TodoTask(title = "Swipeable Task")
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }
        
        // Perform swipe left gesture
        composeTestRule.onNodeWithText("Swipeable Task").performTouchInput {
            swipeLeft()
        }
        
        // Check if action buttons are revealed
        composeTestRule.onNodeWithContentDescription("Edit task").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete task").assertIsDisplayed()
    }

    @Test
    fun swipeableTaskItem_callsEditCallback() {
        val task = TodoTask(title = "Edit Test Task")
        var editedTask: TodoTask? = null
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = { editedTask = it },
                    onDeleteTask = {}
                )
            }
        }
        
        // Swipe to reveal actions
        composeTestRule.onNodeWithText("Edit Test Task").performTouchInput {
            swipeLeft()
        }
        
        // Click edit button
        composeTestRule.onNodeWithContentDescription("Edit task").performClick()
        
        assert(editedTask == task)
    }

    @Test
    fun swipeableTaskItem_callsDeleteCallback() {
        val task = TodoTask(title = "Delete Test Task")
        var deletedTask: TodoTask? = null
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = {},
                    onDeleteTask = { deletedTask = it }
                )
            }
        }
        
        // Swipe to reveal actions
        composeTestRule.onNodeWithText("Delete Test Task").performTouchInput {
            swipeLeft()
        }
        
        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete task").performClick()
        
        assert(deletedTask == task)
    }

    @Test
    fun swipeableTaskItem_callsTaskClickWhenNotSwiped() {
        val task = TodoTask(title = "Click Test Task")
        var clickedTask: TodoTask? = null
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = { clickedTask = it },
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }
        
        // Click task without swiping
        composeTestRule.onNodeWithText("Click Test Task").performClick()
        
        assert(clickedTask == task)
    }

    @Test
    fun swipeableTaskItem_displaysCompletedState() {
        val task = TodoTask(
            title = "Completed Swipeable Task",
            isCompleted = true
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Completed Swipeable Task").assertIsDisplayed()
    }

    @Test
    fun swipeableTaskItem_displaysPriorityAndCategory() {
        val task = TodoTask(
            title = "Priority Task",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = {},
                    onTaskClick = {},
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Priority Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Work").assertIsDisplayed()
    }
}