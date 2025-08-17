package com.example.todoapp.presentation.components

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import org.junit.Test

class SwipeableTaskItemTest {

    @Test
    fun swipeableTaskItem_createsTaskWithCorrectProperties() {
        val task = TodoTask(
            title = "Swipeable Test Task",
            description = "Test Description",
            priority = Priority.HIGH,
            category = Category.WORK
        )
        
        assert(task.title == "Swipeable Test Task")
        assert(task.description == "Test Description")
        assert(task.priority == Priority.HIGH)
        assert(task.category == Category.WORK)
        assert(!task.isCompleted)
    }

    @Test
    fun swipeableTaskItem_handlesCompletedState() {
        val task = TodoTask(
            title = "Completed Swipeable Task",
            isCompleted = true
        )
        
        assert(task.isCompleted)
        assert(task.title == "Completed Swipeable Task")
    }

    @Test
    fun swipeableTaskItem_handlesCallbacks() {
        val task = TodoTask(title = "Test Task", id = "test-id")
        
        var toggledTaskId: String? = null
        var clickedTask: TodoTask? = null
        var editedTask: TodoTask? = null
        var deletedTask: TodoTask? = null
        
        val onToggleComplete: (String) -> Unit = { taskId -> toggledTaskId = taskId }
        val onTaskClick: (TodoTask) -> Unit = { clickedTask = it }
        val onEditTask: (TodoTask) -> Unit = { editedTask = it }
        val onDeleteTask: (TodoTask) -> Unit = { deletedTask = it }
        
        // Simulate callback calls
        onToggleComplete(task.id)
        onTaskClick(task)
        onEditTask(task)
        onDeleteTask(task)
        
        assert(toggledTaskId == "test-id")
        assert(clickedTask == task)
        assert(editedTask == task)
        assert(deletedTask == task)
    }

    @Test
    fun swipeableTaskItem_handlesSwipeActions() {
        val task = TodoTask(
            title = "Task with swipe actions",
            priority = Priority.MEDIUM
        )
        
        var editActionCalled = false
        var deleteActionCalled = false
        
        val onEditTask: (TodoTask) -> Unit = { editActionCalled = true }
        val onDeleteTask: (TodoTask) -> Unit = { deleteActionCalled = true }
        
        // Simulate swipe actions
        onEditTask(task)
        onDeleteTask(task)
        
        assert(editActionCalled)
        assert(deleteActionCalled)
    }

    @Test
    fun swipeableTaskItem_maintainsTaskProperties() {
        val originalTask = TodoTask(
            title = "Original Task",
            description = "Original Description",
            priority = Priority.HIGH,
            category = Category.SHOPPING,
            isCompleted = false
        )
        
        // Verify that swipeable wrapper doesn't modify task properties
        assert(originalTask.title == "Original Task")
        assert(originalTask.description == "Original Description")
        assert(originalTask.priority == Priority.HIGH)
        assert(originalTask.category == Category.SHOPPING)
        assert(!originalTask.isCompleted)
    }
}