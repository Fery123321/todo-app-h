package com.example.todoapp.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchBarUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_displaysPlaceholder() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onClearQuery = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Search tasks...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun searchBar_showsClearButtonWhenQueryNotEmpty() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SearchBar(
                    query = "test query",
                    onQueryChange = {},
                    onClearQuery = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun searchBar_hidesClearButtonWhenQueryEmpty() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onClearQuery = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsNotDisplayed()
    }

    @Test
    fun searchBar_callsOnQueryChangeWhenTextEntered() {
        var capturedQuery = ""

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SearchBar(
                    query = "",
                    onQueryChange = { capturedQuery = it },
                    onClearQuery = {}
                )
            }
        }

        // Perform text input
        composeTestRule.onNodeWithText("Search tasks...").performTextInput("test")

        // Then
        assert(capturedQuery == "test")
    }

    @Test
    fun searchBar_callsOnClearQueryWhenClearButtonClicked() {
        var clearCalled = false

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SearchBar(
                    query = "test query",
                    onQueryChange = {},
                    onClearQuery = { clearCalled = true }
                )
            }
        }

        // Click clear button
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        // Then
        assert(clearCalled)
    }
}