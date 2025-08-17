package com.example.todoapp.presentation.components

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import org.junit.Test
import java.time.LocalDateTime

class TaskItemTest {

    @Test
    fun taskItem_createsTaskWithCorrectProperties() {
        val task = TodoTask(
            title = "Test Task",
            description = "Test Description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        assert(task.title == "Test Task")
        assert(task.description == "Test Description")
        assert(task.priority == Priority.HIGH)
        assert(task.category == Category.WORK)
        assert(!task.isCompleted)
    }

    @Test
    fun taskItem_handlesCompletedState() {
        val task = TodoTask(
            title = "Completed Task",
            isCompleted = true
        )
        
        assert(task.isCompleted)
        assert(task.title == "Completed Task")
    }

    @Test
    fun taskItem_handlesIncompleteState() {
        val task = TodoTask(
            title = "Incomplete Task",
            isCompleted = false
        )
        
        assert(!task.isCompleted)
        assert(task.title == "Incomplete Task")
    }

    @Test
    fun taskItem_handlesDifferentPriorities() {
        val highPriorityTask = TodoTask(title = "High Priority", priority = Priority.HIGH)
        val mediumPriorityTask = TodoTask(title = "Medium Priority", priority = Priority.MEDIUM)
        val lowPriorityTask = TodoTask(title = "Low Priority", priority = Priority.LOW)
        
        assert(highPriorityTask.priority == Priority.HIGH)
        assert(mediumPriorityTask.priority == Priority.MEDIUM)
        assert(lowPriorityTask.priority == Priority.LOW)
    }

    @Test
    fun taskItem_handlesDifferentCategories() {
        val workTask = TodoTask(title = "Work Task", category = Category.WORK)
        val personalTask = TodoTask(title = "Personal Task", category = Category.PERSONAL)
        val shoppingTask = TodoTask(title = "Shopping Task", category = Category.SHOPPING)
        
        assert(workTask.category == Category.WORK)
        assert(personalTask.category == Category.PERSONAL)
        assert(shoppingTask.category == Category.SHOPPING)
    }

    @Test
    fun taskItem_handlesDueDate() {
        val dueDate = LocalDateTime.now().plusDays(1)
        val task = TodoTask(
            title = "Task with due date",
            dueDate = dueDate
        )
        
        assert(task.dueDate == dueDate)
    }

    @Test
    fun taskItem_handlesEmptyDescription() {
        val task = TodoTask(
            title = "Task without description",
            description = ""
        )
        
        assert(task.description.isEmpty())
        assert(task.title == "Task without description")
    }

    @Test
    fun taskItem_hasUniqueId() {
        val task1 = TodoTask(title = "Task 1")
        val task2 = TodoTask(title = "Task 2")
        
        assert(task1.id != task2.id)
        assert(task1.id.isNotEmpty())
        assert(task2.id.isNotEmpty())
    }
}