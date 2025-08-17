package com.example.todoapp.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.presentation.TodoViewModel
import com.example.todoapp.presentation.components.TodoScaffold
import com.example.todoapp.presentation.screen.AddEditTaskScreen
import com.example.todoapp.presentation.screen.StatisticsScreen
import com.example.todoapp.presentation.screen.TaskListScreen

@Composable
fun TodoNavigation(
    navController: NavHostController = rememberNavController()
) {
    val viewModel: TodoViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val navigationState = navController.getCurrentNavigationState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchActive by remember { mutableStateOf(false) }
    
    TodoScaffold(
        navigationState = navigationState,
        onNavigateBack = { navController.navigateBack() },
        onSearchClick = { isSearchActive = !isSearchActive },
        onStatisticsClick = { navController.navigateToStatistics() },
        isSearchActive = isSearchActive,
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = TodoRoutes.TASK_LIST,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(
                route = TodoRoutes.TASK_LIST
            ) {
                TaskListScreen(
                    uiState = uiState,
                    onTaskClick = { task ->
                        navController.navigateToEditTask(task.id)
                    },
                    onAddTaskClick = {
                        navController.navigateToAddTask()
                    },
                    onNavigateToStatistics = {
                        navController.navigateToStatistics()
                    },
                    onTaskToggle = viewModel::toggleTaskCompletion,
                    onTaskDelete = viewModel::deleteTask,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onCategoryFilterChange = viewModel::selectCategory,
                    onCompletionFilterChange = viewModel::setShowCompletedTasks,
                    onClearFilters = viewModel::clearAllFilters
                )
            }
        
        composable(
            route = TodoRoutes.ADD_TASK,
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            AddEditTaskScreen(
                task = null,
                onNavigateBack = {
                    navController.navigateBack()
                },
                onSaveTask = { task ->
                    viewModel.createTask(
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        category = task.category,
                        dueDate = task.dueDate
                    )
                }
            )
        }
        
        composable(
            route = TodoRoutes.EDIT_TASK,
            arguments = listOf(
                androidx.navigation.navArgument(TodoArgs.TASK_ID) {
                    type = androidx.navigation.NavType.StringType
                }
            ),
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(TodoArgs.TASK_ID) ?: ""
            val task = uiState.tasks.find { it.id == taskId }
            
            AddEditTaskScreen(
                task = task,
                onNavigateBack = {
                    navController.navigateBack()
                },
                onSaveTask = { updatedTask ->
                    viewModel.updateTask(updatedTask)
                }
            )
        }
        
        composable(
            route = TodoRoutes.STATISTICS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            StatisticsScreen(
                statistics = uiState.statistics,
                onNavigateBack = {
                    navController.navigateBack()
                }
            )
        }
        }
    }
}