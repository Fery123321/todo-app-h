package com.example.todoapp.domain.model

data class TaskStatistics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val categoryBreakdown: Map<Category, Int> = emptyMap(),
    val weeklyProgress: List<DailyProgress> = emptyList()
)