package com.example.todoapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todoapp.domain.model.Category
import com.example.todoapp.ui.theme.TodoAppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChips(
    selectedCategory: Category?,
    showCompletedTasks: Boolean,
    onCategorySelected: (Category?) -> Unit,
    onShowCompletedToggle: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategory != null || !showCompletedTasks
    
    Column(modifier = modifier) {
        // Filter header with clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (hasActiveFilters) {
                TextButton(onClick = onClearFilters) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Clear all")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Category filters
        Text(
            text = "Categories",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Category.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(
                            if (selectedCategory == category) null else category
                        )
                    },
                    label = {
                        Text(category.displayName)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Status filters
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showCompletedTasks,
                onClick = { onShowCompletedToggle(!showCompletedTasks) },
                label = {
                    Text("Show completed")
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsPreview() {
    TodoAppTheme {
        FilterChips(
            selectedCategory = null,
            showCompletedTasks = true,
            onCategorySelected = {},
            onShowCompletedToggle = {},
            onClearFilters = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsWithSelectionPreview() {
    TodoAppTheme {
        FilterChips(
            selectedCategory = Category.WORK,
            showCompletedTasks = false,
            onCategorySelected = {},
            onShowCompletedToggle = {},
            onClearFilters = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}