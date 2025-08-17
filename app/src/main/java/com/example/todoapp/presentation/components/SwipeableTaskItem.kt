package com.example.todoapp.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.ui.theme.TodoAppTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableTaskItem(
    task: TodoTask,
    onToggleComplete: (String) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onEditTask: (TodoTask) -> Unit,
    onDeleteTask: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX = remember { Animatable(0f) }
    
    // Action button width in pixels
    val actionButtonWidthPx = with(density) { 80.dp.toPx() }
    val maxSwipeDistance = actionButtonWidthPx * 2 // Two buttons
    
    // Threshold for triggering actions
    val swipeThreshold = maxSwipeDistance * 0.3f
    
    LaunchedEffect(offsetX) {
        animatedOffsetX.snapTo(offsetX)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background actions (revealed when swiping)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side spacer to push actions to the right
            Box(modifier = Modifier.weight(1f))
            
            // Edit action
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { 
                        onEditTask(task)
                        // Reset swipe position
                        coroutineScope.launch {
                            animatedOffsetX.animateTo(0f, animationSpec = tween(300))
                            offsetX = 0f
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Delete action
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { 
                        onDeleteTask(task)
                        // Reset swipe position
                        coroutineScope.launch {
                            animatedOffsetX.animateTo(0f, animationSpec = tween(300))
                            offsetX = 0f
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
        
        // Main task item (swipeable)
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // Determine final position based on swipe distance
                                val targetOffset = when {
                                    offsetX < -swipeThreshold -> -maxSwipeDistance
                                    offsetX > swipeThreshold -> 0f
                                    else -> 0f
                                }
                                
                                animatedOffsetX.animateTo(
                                    targetValue = targetOffset,
                                    animationSpec = tween(300)
                                )
                                offsetX = targetOffset
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount).coerceIn(-maxSwipeDistance, 0f)
                        offsetX = newOffset
                        coroutineScope.launch {
                            animatedOffsetX.snapTo(newOffset)
                        }
                    }
                }
        ) {
            TaskItem(
                task = task,
                onToggleComplete = onToggleComplete,
                onTaskClick = { 
                    if (abs(offsetX) < 10f) { // Only trigger click if not swiped
                        onTaskClick(it)
                    } else {
                        // Reset swipe if clicked while swiped
                        coroutineScope.launch {
                            animatedOffsetX.animateTo(0f, animationSpec = tween(300))
                            offsetX = 0f
                        }
                    }
                }
            )
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun SwipeableTaskItemPreview() {
    TodoAppTheme {
        SwipeableTaskItem(
            task = TodoTask(
                title = "Swipeable task",
                description = "Swipe left to reveal actions",
                priority = Priority.MEDIUM,
                category = Category.WORK
            ),
            onToggleComplete = {},
            onTaskClick = {},
            onEditTask = {},
            onDeleteTask = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableTaskItemCompletedPreview() {
    TodoAppTheme {
        SwipeableTaskItem(
            task = TodoTask(
                title = "Completed swipeable task",
                description = "This task is completed",
                isCompleted = true,
                priority = Priority.LOW,
                category = Category.PERSONAL
            ),
            onToggleComplete = {},
            onTaskClick = {},
            onEditTask = {},
            onDeleteTask = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}