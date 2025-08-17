package com.example.todoapp.domain.model

import androidx.compose.ui.graphics.Color
import com.example.todoapp.ui.theme.PriorityHigh
import com.example.todoapp.ui.theme.PriorityMedium
import com.example.todoapp.ui.theme.PriorityLow

enum class Priority(val displayName: String, val color: Color) {
    HIGH("High", PriorityHigh),
    MEDIUM("Medium", PriorityMedium),
    LOW("Low", PriorityLow)
}