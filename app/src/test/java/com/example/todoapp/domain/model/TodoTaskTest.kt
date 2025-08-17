package com.example.todoapp.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class TodoTaskTest {

    @Test
    fun `TodoTask creation with default values`() {
        // Given
        val title = "Test Task"
        val description = "Test Description"

        // When
        val task = TodoTask(
            title = title,
            description = description
        )

        // Then
        assertNotNull(task.id)
        assertEquals(title, task.title)
        assertEquals(description, task.description)
        assertFalse(task.isCompleted)
        assertEquals(Priority.MEDIUM, task.priority)
        assertEquals(Category.PERSONAL, task.category)
        assertNull(task.dueDate)
        assertNotNull(task.createdAt)
        assertNull(task.completedAt)
    }

    @Test
    fun `TodoTask creation with all parameters`() {
        // Given
        val id = "test-id"
        val title = "Test Task"
        val description = "Test Description"
        val isCompleted = true
        val priority = Priority.HIGH
        val category = Category.WORK
        val dueDate = LocalDateTime.now().plusDays(1)
        val createdAt = LocalDateTime.now()
        val completedAt = LocalDateTime.now()

        // When
        val task = TodoTask(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            priority = priority,
            category = category,
            dueDate = dueDate,
            createdAt = createdAt,
            completedAt = completedAt
        )

        // Then
        assertEquals(id, task.id)
        assertEquals(title, task.title)
        assertEquals(description, task.description)
        assertTrue(task.isCompleted)
        assertEquals(priority, task.priority)
        assertEquals(category, task.category)
        assertEquals(dueDate, task.dueDate)
        assertEquals(createdAt, task.createdAt)
        assertEquals(completedAt, task.completedAt)
    }

    @Test
    fun `TodoTask copy with modifications`() {
        // Given
        val originalTask = TodoTask(
            title = "Original Title",
            description = "Original Description"
        )

        // When
        val modifiedTask = originalTask.copy(
            title = "Modified Title",
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )

        // Then
        assertEquals("Modified Title", modifiedTask.title)
        assertEquals("Original Description", modifiedTask.description)
        assertTrue(modifiedTask.isCompleted)
        assertNotNull(modifiedTask.completedAt)
        assertEquals(originalTask.id, modifiedTask.id)
        assertEquals(originalTask.createdAt, modifiedTask.createdAt)
    }
}