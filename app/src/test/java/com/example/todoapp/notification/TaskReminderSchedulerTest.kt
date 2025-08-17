package com.example.todoapp.notification

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TaskReminderSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var scheduler: TaskReminderScheduler

    @Before
    fun setup() {
        workManager = mockk(relaxed = true)
        scheduler = TaskReminderScheduler(workManager)
    }

    @Test
    fun `scheduleTaskReminder schedules work for task with future due date`() {
        // Given
        val futureDate = LocalDateTime.now().plusHours(3)
        val task = TodoTask(
            id = "test-task",
            title = "Test Task",
            dueDate = futureDate
        )

        val workRequestSlot = slot<OneTimeWorkRequest>()
        val workNameSlot = slot<String>()
        val policySlot = slot<ExistingWorkPolicy>()

        // When
        scheduler.scheduleTaskReminder(task)

        // Then
        verify {
            workManager.enqueueUniqueWork(
                capture(workNameSlot),
                capture(policySlot),
                capture(workRequestSlot)
            )
        }

        assert(workNameSlot.captured.contains(task.id))
        assert(policySlot.captured == ExistingWorkPolicy.REPLACE)
    }

    @Test
    fun `scheduleTaskReminder does not schedule for completed task`() {
        // Given
        val futureDate = LocalDateTime.now().plusHours(3)
        val completedTask = TodoTask(
            id = "completed-task",
            title = "Completed Task",
            dueDate = futureDate,
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )

        // When
        scheduler.scheduleTaskReminder(completedTask)

        // Then
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `scheduleTaskReminder does not schedule for overdue task`() {
        // Given
        val pastDate = LocalDateTime.now().minusHours(3)
        val overdueTask = TodoTask(
            id = "overdue-task",
            title = "Overdue Task",
            dueDate = pastDate
        )

        // When
        scheduler.scheduleTaskReminder(overdueTask)

        // Then
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `scheduleTaskReminder does not schedule for task without due date`() {
        // Given
        val taskWithoutDueDate = TodoTask(
            id = "no-due-date",
            title = "Task Without Due Date"
        )

        // When
        scheduler.scheduleTaskReminder(taskWithoutDueDate)

        // Then
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `cancelTaskReminder cancels work for task`() {
        // Given
        val taskId = "test-task"

        // When
        scheduler.cancelTaskReminder(taskId)

        // Then
        verify {
            workManager.cancelUniqueWork("task_reminder_$taskId")
        }
    }

    @Test
    fun `rescheduleTaskReminder cancels and reschedules task`() {
        // Given
        val futureDate = LocalDateTime.now().plusHours(3)
        val task = TodoTask(
            id = "test-task",
            title = "Test Task",
            dueDate = futureDate
        )

        // When
        scheduler.rescheduleTaskReminder(task)

        // Then
        verify {
            workManager.cancelUniqueWork("task_reminder_${task.id}")
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `scheduleAllTaskReminders schedules reminders for all valid tasks`() {
        // Given
        val tasks = listOf(
            TodoTask(
                id = "task1",
                title = "Task 1",
                dueDate = LocalDateTime.now().plusHours(2)
            ),
            TodoTask(
                id = "task2",
                title = "Task 2",
                dueDate = LocalDateTime.now().plusHours(4)
            ),
            TodoTask(
                id = "task3",
                title = "Task 3",
                isCompleted = true,
                dueDate = LocalDateTime.now().plusHours(1)
            )
        )

        // When
        scheduler.scheduleAllTaskReminders(tasks)

        // Then - Should schedule for 2 tasks (excluding completed one)
        verify(exactly = 2) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `cancelAllReminders cancels all work with tag`() {
        // When
        scheduler.cancelAllReminders()

        // Then
        verify {
            workManager.cancelAllWorkByTag("task_reminder_")
        }
    }
}