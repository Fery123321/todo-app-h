package com.example.todoapp.notification

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.util.DateUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        private const val REMINDER_HOURS_BEFORE = 1L // Send reminder 1 hour before due date
        private const val WORK_TAG_PREFIX = "task_reminder_"
    }
    
    /**
     * Schedules a reminder for a task
     */
    fun scheduleTaskReminder(task: TodoTask) {
        val dueDate = task.dueDate ?: return
        
        if (task.isCompleted || DateUtils.isOverdue(dueDate, task.isCompleted)) {
            // Don't schedule reminders for completed or overdue tasks
            return
        }
        
        val reminderTime = dueDate.minusHours(REMINDER_HOURS_BEFORE)
        val now = LocalDateTime.now()
        
        if (reminderTime.isBefore(now)) {
            // Reminder time has already passed, don't schedule
            return
        }
        
        val delayInMillis = java.time.Duration.between(now, reminderTime).toMillis()
        
        val inputData = Data.Builder()
            .putString(TaskReminderWorker.INPUT_TASK_ID, task.id)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(getWorkTag(task.id))
            .build()
        
        workManager.enqueueUniqueWork(
            getWorkName(task.id),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Cancels a scheduled reminder for a task
     */
    fun cancelTaskReminder(taskId: String) {
        workManager.cancelUniqueWork(getWorkName(taskId))
    }
    
    /**
     * Reschedules a reminder for a task (cancels existing and creates new)
     */
    fun rescheduleTaskReminder(task: TodoTask) {
        cancelTaskReminder(task.id)
        scheduleTaskReminder(task)
    }
    
    /**
     * Schedules reminders for all tasks that need them
     */
    fun scheduleAllTaskReminders(tasks: List<TodoTask>) {
        tasks.forEach { task ->
            scheduleTaskReminder(task)
        }
    }
    
    /**
     * Cancels all scheduled reminders
     */
    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(WORK_TAG_PREFIX)
    }
    
    private fun getWorkName(taskId: String): String {
        return "${WORK_TAG_PREFIX}$taskId"
    }
    
    private fun getWorkTag(taskId: String): String {
        return "${WORK_TAG_PREFIX}$taskId"
    }
}