package com.example.todoapp.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.DailyProgress
import com.example.todoapp.domain.model.TaskStatistics
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StatisticsScreenUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun statisticsScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = TaskStatistics(),
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Statistics")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysBackButton() {
        var backPressed = false
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = TaskStatistics(),
                    onNavigateBack = { backPressed = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()
        
        assert(backPressed)
    }
    
    @Test
    fun statisticsScreen_displaysOverviewCards() {
        val statistics = TaskStatistics(
            totalTasks = 15,
            completedTasks = 10,
            completionRate = 66.67f
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Total Tasks")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Completed")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("10")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysCompletionRate() {
        val statistics = TaskStatistics(
            totalTasks = 20,
            completedTasks = 15,
            completionRate = 75.0f
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Completion Rate")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("75%")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysWeeklyProgress() {
        val weeklyProgress = listOf(
            DailyProgress(LocalDate.now().minusDays(6), 2, 3),
            DailyProgress(LocalDate.now().minusDays(5), 1, 2),
            DailyProgress(LocalDate.now().minusDays(4), 3, 4),
            DailyProgress(LocalDate.now().minusDays(3), 2, 2),
            DailyProgress(LocalDate.now().minusDays(2), 1, 3),
            DailyProgress(LocalDate.now().minusDays(1), 4, 5),
            DailyProgress(LocalDate.now(), 2, 3)
        )
        
        val statistics = TaskStatistics(
            weeklyProgress = weeklyProgress
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Weekly Progress")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysCategoryBreakdown() {
        val categoryBreakdown = mapOf(
            Category.WORK to 8,
            Category.PERSONAL to 5,
            Category.SHOPPING to 2
        )
        
        val statistics = TaskStatistics(
            categoryBreakdown = categoryBreakdown
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Category Breakdown")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Work")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("8")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Personal")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("5")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysStreakCard() {
        val statistics = TaskStatistics(
            currentStreak = 5
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("5")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Days Streak")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Keep it up!")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_displaysStreakCardSingular() {
        val statistics = TaskStatistics(
            currentStreak = 1
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Day Streak")
            .assertIsDisplayed()
    }
    
    @Test
    fun statisticsScreen_hidesCategoryBreakdownWhenEmpty() {
        val statistics = TaskStatistics(
            categoryBreakdown = emptyMap()
        )
        
        composeTestRule.setContent {
            TodoAppTheme {
                StatisticsScreen(
                    statistics = statistics,
                    onNavigateBack = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Category Breakdown")
            .assertDoesNotExist()
    }
}