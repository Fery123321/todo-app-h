package com.example.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Navigation utilities for the Todo app
 */

/**
 * Get current navigation state
 */
@Composable
fun NavController.getCurrentNavigationState(): NavigationState {
    val navBackStackEntry by currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TodoDestinations.TASK_LIST
    
    val canNavigateBack = previousBackStackEntry != null
    
    val title = when {
        currentRoute == TodoRoutes.TASK_LIST -> TodoScreenTitles.TASK_LIST
        currentRoute == TodoRoutes.ADD_TASK -> TodoScreenTitles.ADD_TASK
        currentRoute.startsWith(TodoDestinations.EDIT_TASK) -> TodoScreenTitles.EDIT_TASK
        currentRoute == TodoRoutes.STATISTICS -> TodoScreenTitles.STATISTICS
        else -> TodoScreenTitles.TASK_LIST
    }
    
    return NavigationState(
        currentRoute = currentRoute,
        canNavigateBack = canNavigateBack,
        title = title
    )
}

/**
 * Handle system back button
 */
@Composable
fun HandleSystemBack(
    navController: NavController,
    onBackPressed: () -> Unit = { navController.navigateBack() }
) {
    LaunchedEffect(Unit) {
        // This would be handled by the activity's onBackPressedDispatcher
        // in a real implementation
    }
}

/**
 * Navigation event sealed class for handling navigation actions
 */
sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
    object NavigateToTaskList : NavigationEvent()
    object NavigateToAddTask : NavigationEvent()
    data class NavigateToEditTask(val taskId: String) : NavigationEvent()
    object NavigateToStatistics : NavigationEvent()
}

/**
 * Handle navigation events
 */
fun NavController.handleNavigationEvent(event: NavigationEvent) {
    when (event) {
        is NavigationEvent.NavigateBack -> navigateBack()
        is NavigationEvent.NavigateToTaskList -> navigateToTaskListAndClearBackStack()
        is NavigationEvent.NavigateToAddTask -> navigateToAddTask()
        is NavigationEvent.NavigateToEditTask -> navigateToEditTask(event.taskId)
        is NavigationEvent.NavigateToStatistics -> navigateToStatistics()
    }
}

/**
 * Deep link handling utilities
 */
object DeepLinkUtils {
    const val SCHEME = "todoapp"
    const val HOST = "task"
    
    fun createTaskDeepLink(taskId: String): String {
        return "$SCHEME://$HOST/${TodoDestinations.EDIT_TASK}/$taskId"
    }
    
    fun parseTaskIdFromDeepLink(deepLink: String): String? {
        return if (deepLink.startsWith("$SCHEME://$HOST/${TodoDestinations.EDIT_TASK}/")) {
            deepLink.substringAfterLast("/")
        } else {
            null
        }
    }
}