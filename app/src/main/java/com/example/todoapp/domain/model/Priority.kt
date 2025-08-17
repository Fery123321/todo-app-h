package com.example.todoapp.domain.model

import androidx.compose.ui.graphics.Color

enum class Priority(val displayName: String, val color: Color) {
    HIGH("High", Color(0xFFE53E3E)),
    MEDIUM("Medium", Color(0xFFFF8C00)),
    LOW("Low", Color(0xFF38A169))
}