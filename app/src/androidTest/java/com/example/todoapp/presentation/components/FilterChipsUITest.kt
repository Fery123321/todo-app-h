package com.example.todoapp.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.domain.model.Category
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterChipsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun filterChips_displaysAllCategories() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = null,
                    showCompletedTasks = true,
                    onCategorySelected = {},
                    onShowCompletedToggle = {},
                    onClearFilters = {}
                )
            }
        }

        // Then
        Category.entries.forEach { category ->
            composeTestRule.onNodeWithText(category.displayName).assertIsDisplayed()
        }
    }

    @Test
    fun filterChips_displaysStatusFilter() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = null,
                    showCompletedTasks = true,
                    onCategorySelected = {},
                    onShowCompletedToggle = {},
                    onClearFilters = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Show completed").assertIsDisplayed()
    }

    @Test
    fun filterChips_showsClearButtonWhenFiltersActive() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = Category.WORK,
                    showCompletedTasks = true,
                    onCategorySelected = {},
                    onShowCompletedToggle = {},
                    onClearFilters = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Clear all").assertIsDisplayed()
    }

    @Test
    fun filterChips_hidesClearButtonWhenNoFiltersActive() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = null,
                    showCompletedTasks = true,
                    onCategorySelected = {},
                    onShowCompletedToggle = {},
                    onClearFilters = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Clear all").assertIsNotDisplayed()
    }

    @Test
    fun filterChips_callsOnCategorySelectedWhenCategoryClicked() {
        var selectedCategory: Category? = null

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = null,
                    showCompletedTasks = true,
                    onCategorySelected = { selectedCategory = it },
                    onShowCompletedToggle = {},
                    onClearFilters = {}
                )
            }
        }

        // Click on Work category
        composeTestRule.onNodeWithText("Work").performClick()

        // Then
        assert(selectedCategory == Category.WORK)
    }

    @Test
    fun filterChips_callsOnShowCompletedToggleWhenStatusClicked() {
        var toggleCalled = false

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = null,
                    showCompletedTasks = true,
                    onCategorySelected = {},
                    onShowCompletedToggle = { toggleCalled = true },
                    onClearFilters = {}
                )
            }
        }

        // Click on show completed
        composeTestRule.onNodeWithText("Show completed").performClick()

        // Then
        assert(toggleCalled)
    }

    @Test
    fun filterChips_callsOnClearFiltersWhenClearButtonClicked() {
        var clearCalled = false

        // When
        composeTestRule.setContent {
            TodoAppTheme {
                FilterChips(
                    selectedCategory = Category.WORK,
                    showCompletedTasks = false,
                    onCategorySelected = {},
                    onShowCompletedToggle = {},
                    onClearFilters = { clearCalled = true }
                )
            }
        }

        // Click clear all button
        composeTestRule.onNodeWithText("Clear all").performClick()

        // Then
        assert(clearCalled)
    }
}