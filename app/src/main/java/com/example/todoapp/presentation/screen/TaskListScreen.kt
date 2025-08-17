package com.example.todoapp.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.presentation.TodoUiState
import com.example.todoapp.presentation.components.FilterChips
import com.example.todoapp.presentation.components.SearchBar
import com.example.todoapp.presentation.components.OptimizedTaskList
import com.example.todoapp.presentation.components.SkeletonTaskList
import com.example.todoapp.presentation.components.SwipeableTaskItem
import com.example.todoapp.presentation.components.TaskInputDialog
import com.example.todoapp.ui.theme.TodoAppTheme
import com.example.todoapp.util.PerformanceMonitor
import com.example.todoapp.util.rememberHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiState: com.example.todoapp.presentation.TodoUiState,
    onTaskClick: (TodoTask) -> Unit,
    onAddTaskClick: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onTaskToggle: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (Category?) -> Unit,
    onCompletionFilterChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    performanceMonitor: PerformanceMonitor? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState()
    val haptic = rememberHapticFeedback()
    
    // Show error messages in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { 
                        haptic.lightTap()
                        onNavigateToStatistics() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "View statistics"
                        )
                    }
                    IconButton(onClick = { 
                        haptic.lightTap()
                        showFilterSheet = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Filter tasks"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    haptic.mediumTap()
                    onAddTaskClick()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearQuery = { onSearchQueryChange("") },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Animated content based on state
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                SkeletonTaskList()
            }
            
            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.tasks.isEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 4 }
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it / 4 }
                )
            ) {
                EmptyTasksContent(
                    hasFilters = uiState.searchQuery.isNotEmpty() || 
                               uiState.selectedCategory != null || 
                               !uiState.showCompletedTasks
                )
            }
            
            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.tasks.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 4 }
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it / 4 }
                )
            ) {
                OptimizedTaskList(
                    tasks = uiState.tasks,
                    isLoading = uiState.isLoading,
                    onTaskClick = onTaskClick,
                    onTaskToggle = onTaskToggle,
                    onTaskDelete = onTaskDelete,
                    onTaskEdit = onTaskClick,
                    performanceMonitor = performanceMonitor,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState
        ) {
            FilterChips(
                selectedCategory = uiState.selectedCategory,
                showCompletedTasks = uiState.showCompletedTasks,
                onCategorySelected = onCategoryFilterChange,
                onShowCompletedToggle = onCompletionFilterChange,
                onClearFilters = {
                    onClearFilters()
                    showFilterSheet = false
                },
                modifier = Modifier.padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp)) // Bottom padding for sheet
        }
    }
    

}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyTasksContent(hasFilters: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (hasFilters) "No tasks match your filters" else "No tasks yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasFilters) {
                    "Try adjusting your search or filters"
                } else {
                    "Tap the + button to create your first task"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 0.dp)
            )
        }
    }
}

@Composable
private fun TaskListContent(
    tasks: List<TodoTask>,
    onToggleComplete: (String) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onEditTask: (TodoTask) -> Unit,
    onDeleteTask: (TodoTask) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = tasks,
            key = { task -> task.id }
        ) { task ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) + 
                       scaleIn(
                           animationSpec = tween(300),
                           initialScale = 0.8f
                       ) +
                       slideInVertically(
                           animationSpec = tween(300),
                           initialOffsetY = { it / 3 }
                       ),
                exit = fadeOut(animationSpec = tween(200)) + 
                      scaleOut(
                          animationSpec = tween(200),
                          targetScale = 0.8f
                      ) +
                      slideOutVertically(
                          animationSpec = tween(200),
                          targetOffsetY = { -it / 3 }
                      )
            ) {
                SwipeableTaskItem(
                    task = task,
                    onToggleComplete = onToggleComplete,
                    onTaskClick = onTaskClick,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun TaskListScreenPreview() {
    TodoAppTheme {
        TaskListScreen(
            uiState = TodoUiState(),
            onTaskClick = {},
            onAddTaskClick = {},
            onNavigateToStatistics = {},
            onTaskToggle = {},
            onTaskDelete = {},
            onSearchQueryChange = {},
            onCategoryFilterChange = {},
            onCompletionFilterChange = {},
            onClearFilters = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListContentPreview() {
    TodoAppTheme {
        TaskListContent(
            tasks = listOf(
                TodoTask(
                    title = "Complete project proposal",
                    description = "Write and review the project proposal document",
                    priority = Priority.HIGH,
                    category = Category.WORK
                ),
                TodoTask(
                    title = "Buy groceries",
                    description = "Milk, bread, eggs, and vegetables",
                    priority = Priority.MEDIUM,
                    category = Category.PERSONAL,
                    isCompleted = true
                ),
                TodoTask(
                    title = "Exercise",
                    description = "30 minutes cardio workout",
                    priority = Priority.LOW,
                    category = Category.PERSONAL
                )
            ),
            onToggleComplete = {},
            onTaskClick = {},
            onEditTask = {},
            onDeleteTask = {}
        )
    }
}