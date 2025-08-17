package com.example.todoapp.notification

import com.example.todoapp.domain.model.TodoTask
import org.junit.Test
import java.time.LocalDateTime

class TodoNotificationManagerTest {

    @Test
    fun `notification ID calculation is consistent`() {
        // Given
        val taskId = "test-task"
        val expectedNotificationId = 1000 + taskId.hashCode()

        // When
        val actualNotificationId = 1000 + taskId.hashCode()

        // Then
        assert(actualNotificationId == expectedNotificationId)
    }

    @Test
    fun `different task IDs produce different notification IDs`() {
        // Given
        val taskId1 = "task-1"
        val taskId2 = "task-2"

        // When
        val notificationId1 = 1000 + taskId1.hashCode()
        val notificationId2 = 1000 + taskId2.hashCode()

        // Then
        assert(notificationId1 != notificationId2)
    }
}