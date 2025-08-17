package com.example.todoapp.data.mapper

import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.Category

fun TaskEntity.toDomainModel(): TodoTask {
    return TodoTask(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = Priority.valueOf(priority),
        category = Category.valueOf(category),
        dueDate = dueDate,
        createdAt = createdAt,
        completedAt = completedAt
    )
}

fun TodoTask.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority.name,
        category = category.name,
        dueDate = dueDate,
        createdAt = createdAt,
        completedAt = completedAt
    )
}