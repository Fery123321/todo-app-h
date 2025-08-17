package com.example.todoapp.integration

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class TaskCreationTest {

    @Test
    fun testTaskCreation() {
        // Test basic task creation
        val task = TodoTask(
            title = "Test Task",
            description = "Test Description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        assertNotNull(task.id)
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(Category.WORK, task.category)
        assertFalse(task.isCompleted)
        assertNotNull(task.createdAt)
        assertNull(task.completedAt)
    }
    
    @Test
    fun testTaskWithDueDate() {
        val dueDate = LocalDateTime.now().plusDays(1)
        val task = TodoTask(
            title = "Task with due date",
            dueDate = dueDate
        )
        
        assertEquals(dueDate, task.dueDate)
    }
    
    @Test
    fun testTaskDefaults() {
        val task = TodoTask(title = "Minimal Task")
        
        assertEquals("", task.description)
        assertEquals(Priority.MEDIUM, task.priority)
        assertEquals(Category.PERSONAL, task.category)
        assertFalse(task.isCompleted)
        assertNull(task.dueDate)
        assertNull(task.completedAt)
    }
}