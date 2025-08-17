package com.example.todoapp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.util.PerformanceMonitor

/**
 * Optimized task list with performance monitoring and efficient rendering
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptimizedTaskList(
    tasks: List<TodoTask>,
    isLoading: Boolean,
    onTaskClick: (TodoTask) -> Unit,
    onTaskToggle: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onTaskEdit: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    performanceMonitor: PerformanceMonitor? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Monitor list performance
    val visibleItemsInfo by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
        }
    }
    
    LaunchedEffect(tasks.size, visibleItemsInfo.size) {
        performanceMonitor?.logListPerformance(
            listName = "TaskList",
            itemCount = tasks.size,
            visibleItemCount = visibleItemsInfo.size,
            scrollOffset = listState.firstVisibleItemScrollOffset
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && tasks.isEmpty() -> {
                // Show loading indicator for initial load
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            tasks.isEmpty() -> {
                // Show empty state
                Text(
                    text = "No tasks found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = tasks,
                        key = { _, task -> task.id }
                    ) { index, task ->
                        // Use optimized task item with stable keys
                        OptimizedTaskItem(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onToggle = { onTaskToggle(task.id) },
                            onDelete = { onTaskDelete(task.id) },
                            onEdit = { onTaskEdit(task) },
                            performanceMonitor = performanceMonitor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                        )
                    }
                    
                    // Add loading indicator at the bottom if loading more items
                    if (isLoading && tasks.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Optimized task item with performance monitoring
 */
@Composable
private fun OptimizedTaskItem(
    task: TodoTask,
    onClick: (TodoTask) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (TodoTask) -> Unit,
    onEdit: (TodoTask) -> Unit,
    performanceMonitor: PerformanceMonitor?,
    modifier: Modifier = Modifier
) {
    // Measure composition time for performance monitoring
    performanceMonitor?.measureTime("TaskItem Composition") {
        SwipeableTaskItem(
            task = task,
            onToggleComplete = { onToggle(task.id) },
            onTaskClick = { onClick(task) },
            onEditTask = { onEdit(task) },
            onDeleteTask = { onDelete(task) },
            modifier = modifier
        )
    } ?: SwipeableTaskItem(
        task = task,
        onToggleComplete = { onToggle(task.id) },
        onTaskClick = { onClick(task) },
        onEditTask = { onEdit(task) },
        onDeleteTask = { onDelete(task) },
        modifier = modifier
    )
}

/**
 * Pagination helper for large lists
 */
data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val isLoading: Boolean = false,
    val hasMoreItems: Boolean = true
)

/**
 * Paginated task list for handling large datasets
 */
@Composable
fun PaginatedTaskList(
    tasks: List<TodoTask>,
    paginationState: PaginationState,
    onLoadMore: () -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onTaskToggle: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onTaskEdit: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    performanceMonitor: PerformanceMonitor? = null
) {
    val listState = rememberLazyListState()
    
    // Trigger load more when approaching end of list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()
                if (lastVisibleItem != null && 
                    lastVisibleItem.index >= tasks.size - 5 && // Load more when 5 items from end
                    paginationState.hasMoreItems && 
                    !paginationState.isLoading) {
                    onLoadMore()
                }
            }
    }
    
    OptimizedTaskList(
        tasks = tasks,
        isLoading = paginationState.isLoading,
        onTaskClick = onTaskClick,
        onTaskToggle = onTaskToggle,
        onTaskDelete = onTaskDelete,
        onTaskEdit = onTaskEdit,
        modifier = modifier,
        performanceMonitor = performanceMonitor,
        listState = listState
    )
}