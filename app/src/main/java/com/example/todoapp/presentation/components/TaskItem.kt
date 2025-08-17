package com.example.todoapp.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.ui.theme.TodoAppTheme
import com.example.todoapp.util.AnimationSpecs
import com.example.todoapp.util.DateUtils
import com.example.todoapp.util.bouncyClickable
import com.example.todoapp.util.rememberHapticFeedback
import java.time.LocalDateTime

@Composable
fun TaskItem(
    task: TodoTask,
    onToggleComplete: (String) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val textAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.6f else 1f,
        animationSpec = AnimationSpecs.mediumFade,
        label = "text_alpha"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (task.isCompleted) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "background_color"
    )
    
    val cardElevation by animateFloatAsState(
        targetValue = if (task.isCompleted) 1f else 3f,
        animationSpec = AnimationSpecs.mediumSpring,
        label = "card_elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable { 
                haptic.lightTap()
                onTaskClick(task) 
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox with haptic feedback
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { 
                    if (it) {
                        haptic.success()
                    } else {
                        haptic.lightTap()
                    }
                    onToggleComplete(task.id) 
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title with priority indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Priority indicator
                    PriorityIndicator(
                        priority = task.priority,
                        alpha = textAlpha
                    )
                }
                
                // Description
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Due date and category row
                if (task.dueDate != null || task.category != Category.PERSONAL) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Due date
                        task.dueDate?.let { dueDate ->
                            DueDateIndicator(
                                dueDate = dueDate,
                                isCompleted = task.isCompleted,
                                alpha = textAlpha
                            )
                        }
                        
                        // Category indicator
                        if (task.category != Category.PERSONAL) {
                            CategoryIndicator(
                                category = task.category,
                                alpha = textAlpha
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityIndicator(
    priority: Priority,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(
                color = priority.color.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

@Composable
private fun CategoryIndicator(
    category: Category,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
    }
}

@Composable
private fun DueDateIndicator(
    dueDate: LocalDateTime,
    isCompleted: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val dueDateStatus = DateUtils.getDueDateStatus(dueDate, isCompleted)
    
    val indicatorColor = when (dueDateStatus) {
        DateUtils.DueDateStatus.OVERDUE -> Color(0xFFE53E3E) // Red
        DateUtils.DueDateStatus.DUE_TODAY -> Color(0xFFFF8C00) // Orange
        DateUtils.DueDateStatus.DUE_SOON -> Color(0xFFFFA500) // Yellow
        DateUtils.DueDateStatus.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }.copy(alpha = alpha)
    
    Text(
        text = DateUtils.formatShortDate(dueDate),
        style = MaterialTheme.typography.labelSmall,
        color = indicatorColor,
        modifier = modifier
    )
}

// Preview functions for different states
@Preview(showBackground = true)
@Composable
private fun TaskItemPreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Complete project documentation",
                description = "Write comprehensive documentation for the new feature",
                priority = Priority.HIGH,
                category = Category.WORK,
                dueDate = LocalDateTime.now().plusDays(2)
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemCompletedPreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Buy groceries",
                description = "Milk, bread, eggs, and vegetables",
                isCompleted = true,
                priority = Priority.MEDIUM,
                category = Category.SHOPPING,
                completedAt = LocalDateTime.now().minusHours(2)
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemOverduePreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Submit tax documents",
                description = "Gather all necessary documents and submit online",
                priority = Priority.HIGH,
                category = Category.PERSONAL,
                dueDate = LocalDateTime.now().minusDays(1)
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemDueTodayPreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Doctor appointment",
                description = "Annual checkup at 3 PM",
                priority = Priority.HIGH,
                category = Category.HEALTH,
                dueDate = LocalDateTime.now().withHour(15).withMinute(0)
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemMinimalPreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Quick task",
                priority = Priority.LOW
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TaskItemDarkPreview() {
    TodoAppTheme {
        TaskItem(
            task = TodoTask(
                title = "Dark theme task",
                description = "This is how it looks in dark mode",
                priority = Priority.MEDIUM,
                category = Category.WORK,
                dueDate = LocalDateTime.now().plusDays(1)
            ),
            onToggleComplete = {},
            onTaskClick = {}
        )
    }
}