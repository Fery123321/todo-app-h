package com.example.todoapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val category: String,
    val dueDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
)