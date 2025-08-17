package com.example.todoapp.presentation.navigation

/**
 * Navigation destinations for the Todo app
 */
object TodoDestinations {
    const val TASK_LIST = "task_list"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task"
    const val STATISTICS = "statistics"
}

/**
 * Navigation routes with parameters
 */
object TodoRoutes {
    const val TASK_LIST = TodoDestinations.TASK_LIST
    const val ADD_TASK = TodoDestinations.ADD_TASK
    const val EDIT_TASK = "${TodoDestinations.EDIT_TASK}/{${TodoArgs.TASK_ID}}"
    const val STATISTICS = TodoDestinations.STATISTICS
}

/**
 * Navigation arguments
 */
object TodoArgs {
    const val TASK_ID = "taskId"
}

/**
 * Navigation route builders
 */
object TodoRouteBuilders {
    fun editTask(taskId: String): String {
        return "${TodoDestinations.EDIT_TASK}/$taskId"
    }
}

/**
 * Screen titles for navigation
 */
object TodoScreenTitles {
    const val TASK_LIST = "Tasks"
    const val ADD_TASK = "Add Task"
    const val EDIT_TASK = "Edit Task"
    const val STATISTICS = "Statistics"
}

/**
 * Navigation state holder
 */
data class NavigationState(
    val currentRoute: String = TodoDestinations.TASK_LIST,
    val canNavigateBack: Boolean = false,
    val title: String = TodoScreenTitles.TASK_LIST
)