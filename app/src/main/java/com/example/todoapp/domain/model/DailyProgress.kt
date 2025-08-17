package com.example.todoapp.domain.model

import java.time.LocalDate

data class DailyProgress(
    val date: LocalDate,
    val completedTasks: Int,
    val totalTasks: Int
)