package com.example.todoapp.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for TodoTopAppBar component
 */
@RunWith(AndroidJUnit4::class)
class TodoTopAppBarUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun todoTopAppBar_displaysTitle() {
        val title = "Test Title"

        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = title
                )
            }
        }

        composeTestRule
            .onNodeWithText(title)
            .assertIsDisplayed()
    }

    @Test
    fun todoTopAppBar_withBackNavigation_showsBackButton() {
        var backClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = "Test",
                    canNavigateBack = true,
                    onNavigateBack = { backClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .performClick()

        assert(backClicked)
    }

    @Test
    fun todoTopAppBar_withSearchAction_showsSearchButton() {
        var searchClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = "Test",
                    showSearchAction = true,
                    onSearchClick = { searchClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Search tasks")
            .assertIsDisplayed()
            .performClick()

        assert(searchClicked)
    }

    @Test
    fun todoTopAppBar_withStatisticsAction_showsStatisticsButton() {
        var statisticsClicked = false

        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = "Test",
                    showStatisticsAction = true,
                    onStatisticsClick = { statisticsClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("View statistics")
            .assertIsDisplayed()
            .performClick()

        assert(statisticsClicked)
    }

    @Test
    fun todoTopAppBar_whenSearchActive_isNotVisible() {
        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = "Test",
                    isSearchActive = true
                )
            }
        }

        // When search is active, the top app bar should not be visible
        composeTestRule
            .onNodeWithText("Test")
            .assertDoesNotExist()
    }

    @Test
    fun todoTopAppBar_withAllActions_showsAllButtons() {
        composeTestRule.setContent {
            TodoAppTheme {
                TodoTopAppBar(
                    title = "Test",
                    canNavigateBack = true,
                    showSearchAction = true,
                    showStatisticsAction = true
                )
            }
        }

        // Verify all buttons are displayed
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Search tasks")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("View statistics")
            .assertIsDisplayed()
    }
}