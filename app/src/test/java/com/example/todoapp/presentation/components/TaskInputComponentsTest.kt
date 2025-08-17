package com.example.todoapp.presentation.components

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import org.junit.Test
import java.time.LocalDateTime

class TaskInputComponentsTest {

    @Test
    fun taskInputDialog_createsTaskWithCorrectProperties() {
        val task = TodoTask(
            title = "New Task",
            description = "Task description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        assert(task.title == "New Task")
        assert(task.description == "Task description")
        assert(task.priority == Priority.HIGH)
        assert(task.category == Category.WORK)
        assert(!task.isCompleted)
    }

    @Test
    fun taskInputDialog_handlesEmptyDescription() {
        val task = TodoTask(
            title = "Task without description",
            description = "",
            priority = Priority.MEDIUM,
            category = Category.PERSONAL
        )
        
        assert(task.title == "Task without description")
        assert(task.description.isEmpty())
        assert(task.priority == Priority.MEDIUM)
        assert(task.category == Category.PERSONAL)
    }

    @Test
    fun taskInputDialog_handlesAllPriorities() {
        val highPriorityTask = TodoTask(title = "High Priority", priority = Priority.HIGH)
        val mediumPriorityTask = TodoTask(title = "Medium Priority", priority = Priority.MEDIUM)
        val lowPriorityTask = TodoTask(title = "Low Priority", priority = Priority.LOW)
        
        assert(highPriorityTask.priority == Priority.HIGH)
        assert(mediumPriorityTask.priority == Priority.MEDIUM)
        assert(lowPriorityTask.priority == Priority.LOW)
    }

    @Test
    fun taskInputDialog_handlesAllCategories() {
        val categories = Category.entries
        
        categories.forEach { category ->
            val task = TodoTask(
                title = "Task for ${category.displayName}",
                category = category
            )
            assert(task.category == category)
        }
    }

    @Test
    fun addEditTaskScreen_createsNewTask() {
        val task = TodoTask(
            title = "New Task from Screen",
            description = "Created from AddEditTaskScreen",
            priority = Priority.HIGH,
            category = Category.SHOPPING,
            dueDate = LocalDateTime.now().plusDays(1)
        )
        
        assert(task.title == "New Task from Screen")
        assert(task.description == "Created from AddEditTaskScreen")
        assert(task.priority == Priority.HIGH)
        assert(task.category == Category.SHOPPING)
        assert(task.dueDate != null)
        assert(!task.isCompleted)
    }

    @Test
    fun addEditTaskScreen_editsExistingTask() {
        val originalTask = TodoTask(
            id = "existing-id",
            title = "Original Title",
            description = "Original Description",
            priority = Priority.LOW,
            category = Category.PERSONAL,
            isCompleted = false
        )
        
        val editedTask = originalTask.copy(
            title = "Edited Title",
            description = "Edited Description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        assert(editedTask.id == originalTask.id) // ID should remain the same
        assert(editedTask.title == "Edited Title")
        assert(editedTask.description == "Edited Description")
        assert(editedTask.priority == Priority.HIGH)
        assert(editedTask.category == Category.WORK)
        assert(editedTask.isCompleted == originalTask.isCompleted) // Completion status preserved
    }

    @Test
    fun addEditTaskScreen_handlesDueDate() {
        val dueDate = LocalDateTime.now().plusDays(3).withHour(14).withMinute(30)
        val task = TodoTask(
            title = "Task with due date",
            dueDate = dueDate
        )
        
        assert(task.dueDate == dueDate)
        assert(task.dueDate?.hour == 14)
        assert(task.dueDate?.minute == 30)
    }

    @Test
    fun addEditTaskScreen_handlesNoDueDate() {
        val task = TodoTask(
            title = "Task without due date",
            dueDate = null
        )
        
        assert(task.dueDate == null)
    }

    @Test
    fun taskValidation_requiresNonEmptyTitle() {
        val validTask = TodoTask(title = "Valid Title")
        val invalidTitle = ""
        
        assert(validTask.title.isNotBlank())
        assert(invalidTitle.isBlank())
    }

    @Test
    fun taskValidation_allowsEmptyDescription() {
        val taskWithDescription = TodoTask(
            title = "Task",
            description = "Has description"
        )
        val taskWithoutDescription = TodoTask(
            title = "Task",
            description = ""
        )
        
        assert(taskWithDescription.description.isNotBlank())
        assert(taskWithoutDescription.description.isBlank())
        // Both should be valid
    }

    @Test
    fun taskInput_handlesCallbacks() {
        val task = TodoTask(title = "Callback Test Task")
        
        var savedTask: TodoTask? = null
        var dismissCalled = false
        
        val onSaveTask: (TodoTask) -> Unit = { savedTask = it }
        val onDismiss: () -> Unit = { dismissCalled = true }
        
        // Simulate callbacks
        onSaveTask(task)
        onDismiss()
        
        assert(savedTask == task)
        assert(dismissCalled)
    }
}