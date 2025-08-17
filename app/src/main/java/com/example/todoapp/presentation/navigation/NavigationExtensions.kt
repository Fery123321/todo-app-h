package com.example.todoapp.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions

/**
 * Extension functions for navigation with improved back stack management
 */

/**
 * Navigate to add task screen with proper options
 */
fun NavController.navigateToAddTask() {
    navigate(TodoDestinations.ADD_TASK) {
        launchSingleTop = true
    }
}

/**
 * Navigate to edit task screen with proper options
 */
fun NavController.navigateToEditTask(taskId: String) {
    navigate("${TodoDestinations.EDIT_TASK}/$taskId") {
        launchSingleTop = true
    }
}

/**
 * Navigate to statistics screen with proper options
 */
fun NavController.navigateToStatistics() {
    navigate(TodoDestinations.STATISTICS) {
        launchSingleTop = true
    }
}

/**
 * Navigate back to task list and clear back stack
 */
fun NavController.navigateToTaskListAndClearBackStack() {
    navigate(TodoDestinations.TASK_LIST) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigate back with proper back stack management
 */
fun NavController.navigateBack() {
    if (!popBackStack()) {
        // If no back stack, navigate to task list
        navigateToTaskListAndClearBackStack()
    }
}

/**
 * Navigate with custom options
 */
fun NavController.navigateWithOptions(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    saveState: Boolean = true,
    restoreState: Boolean = true,
    launchSingleTop: Boolean = true
) {
    navigate(route) {
        popUpToRoute?.let { popUpTo ->
            popUpTo(popUpTo) {
                this.inclusive = inclusive
                this.saveState = saveState
            }
        }
        this.launchSingleTop = launchSingleTop
        this.restoreState = restoreState
    }
}

/**
 * Check if current destination is the given route
 */
fun NavController.isCurrentDestination(route: String): Boolean {
    return currentDestination?.route == route
}

/**
 * Safe navigation that checks if destination exists
 */
fun NavController.safeNavigate(route: String, navOptions: NavOptions? = null) {
    try {
        if (navOptions != null) {
            navigate(route, navOptions)
        } else {
            navigate(route)
        }
    } catch (e: IllegalArgumentException) {
        // Handle navigation error gracefully
        // Log error or show user feedback
    }
}

/**
 * Navigate up with fallback to task list
 */
fun NavController.navigateUpWithFallback() {
    if (!navigateUp()) {
        navigateToTaskListAndClearBackStack()
    }
}