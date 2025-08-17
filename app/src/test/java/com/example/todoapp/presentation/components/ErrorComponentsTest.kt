package com.example.todoapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for error handling components
 */
@RunWith(AndroidJUnit4::class)
class ErrorComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_displaysCorrectContent() {
        val title = "Test Error"
        val message = "This is a test error message"
        var retryClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                ErrorState(
                    title = title,
                    message = message,
                    icon = Icons.Default.Error,
                    onRetry = { retryClicked = true }
                )
            }
        }

        // Verify title and message are displayed
        composeTestRule
            .onNodeWithText(title)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(message)
            .assertIsDisplayed()

        // Verify retry button is displayed and clickable
        composeTestRule
            .onNodeWithText("Try Again")
            .assertIsDisplayed()
            .performClick()

        assert(retryClicked)
    }

    @Test
    fun errorState_withDismissAction_showsDismissButton() {
        var dismissClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                ErrorState(
                    title = "Test Error",
                    message = "Test message",
                    onDismiss = { dismissClicked = true }
                )
            }
        }

        // Verify dismiss button is displayed and clickable
        composeTestRule
            .onNodeWithText("Dismiss")
            .assertIsDisplayed()
            .performClick()

        assert(dismissClicked)
    }

    @Test
    fun networkErrorState_displaysCorrectContent() {
        var retryClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                NetworkErrorState(
                    onRetry = { retryClicked = true }
                )
            }
        }

        // Verify network error specific content
        composeTestRule
            .onNodeWithText("No Internet Connection")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Please check your internet connection and try again.")
            .assertIsDisplayed()

        // Verify retry functionality
        composeTestRule
            .onNodeWithText("Try Again")
            .performClick()

        assert(retryClicked)
    }

    @Test
    fun emptyState_displaysCorrectContent() {
        val title = "No Items"
        val message = "Add some items to get started"
        val actionText = "Add Item"
        var actionClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                EmptyState(
                    title = title,
                    message = message,
                    icon = Icons.Default.Warning,
                    actionText = actionText,
                    onAction = { actionClicked = true }
                )
            }
        }

        // Verify content is displayed
        composeTestRule
            .onNodeWithText(title)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(message)
            .assertIsDisplayed()

        // Verify action button works
        composeTestRule
            .onNodeWithText(actionText)
            .assertIsDisplayed()
            .performClick()

        assert(actionClicked)
    }

    @Test
    fun emptyState_withoutAction_doesNotShowActionButton() {
        composeTestRule.setContent {
            TodoAppTheme {
                EmptyState(
                    title = "No Items",
                    message = "No action available"
                )
            }
        }

        // Verify no action button is displayed
        composeTestRule
            .onNodeWithText("Add Item")
            .assertDoesNotExist()
    }
}