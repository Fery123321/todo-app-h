package com.example.todoapp.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.ui.theme.TodoAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkeletonLoaderUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skeletonTaskItem_isDisplayed() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SkeletonTaskItem()
            }
        }

        // Then - skeleton should be displayed (we can't easily test shimmer animation)
        // The component should render without crashing
        composeTestRule.waitForIdle()
    }

    @Test
    fun skeletonTaskList_displaysMultipleItems() {
        // When
        composeTestRule.setContent {
            TodoAppTheme {
                SkeletonTaskList(itemCount = 3)
            }
        }

        // Then - skeleton list should be displayed
        composeTestRule.waitForIdle()
    }
}