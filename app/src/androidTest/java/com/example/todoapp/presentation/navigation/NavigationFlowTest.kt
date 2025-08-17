package com.example.todoapp.presentation.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.ui.theme.TodoAppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for navigation flows in the Todo app
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            TodoAppTheme {
                TodoNavigation(navController = navController)
            }
        }
    }

    @Test
    fun navigationFlow_startsAtTaskListScreen() {
        // Verify that the app starts at the task list screen
        composeTestRule
            .onNodeWithText("Tasks")
            .assertIsDisplayed()
    }

    @Test
    fun navigationFlow_navigateToAddTaskScreen() {
        // Click on add task button
        composeTestRule
            .onNodeWithContentDescription("Add task")
            .performClick()

        // Verify navigation to add task screen
        composeTestRule
            .onNodeWithText("Add Task")
            .assertIsDisplayed()
    }

    @Test
    fun navigationFlow_navigateToStatisticsScreen() {
        // Click on statistics button
        composeTestRule
            .onNodeWithContentDescription("View statistics")
            .performClick()

        // Verify navigation to statistics screen
        composeTestRule
            .onNodeWithText("Statistics")
            .assertIsDisplayed()
    }

    @Test
    fun navigationFlow_backNavigationFromAddTask() {
        // Navigate to add task screen
        composeTestRule
            .onNodeWithContentDescription("Add task")
            .performClick()

        // Click back button
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .performClick()

        // Verify back navigation to task list
        composeTestRule
            .onNodeWithText("Tasks")
            .assertIsDisplayed()
    }

    @Test
    fun navigationFlow_backNavigationFromStatistics() {
        // Navigate to statistics screen
        composeTestRule
            .onNodeWithContentDescription("View statistics")
            .performClick()

        // Click back button
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .performClick()

        // Verify back navigation to task list
        composeTestRule
            .onNodeWithText("Tasks")
            .assertIsDisplayed()
    }

    @Test
    fun navigationFlow_searchToggle() {
        // Click on search button
        composeTestRule
            .onNodeWithContentDescription("Search tasks")
            .performClick()

        // Verify search is active (top bar should be hidden)
        // This would need to be implemented based on the actual search UI behavior
    }
}