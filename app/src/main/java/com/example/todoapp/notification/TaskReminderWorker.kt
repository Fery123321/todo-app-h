package com.example.todoapp.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val todoRepository: TodoRepository,
    private val notificationManager: TodoNotificationManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "task_reminder_work"
        const val INPUT_TASK_ID = "task_id"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getString(INPUT_TASK_ID)
            if (taskId == null) {
                return Result.failure()
            }
            
            // Get all tasks and find the specific task
            val tasks = todoRepository.getAllTasks().first()
            val task = tasks.find { it.id == taskId }
            
            if (task == null || task.isCompleted) {
                // Task doesn't exist or is already completed
                return Result.success()
            }
            
            val dueDate = task.dueDate
            if (dueDate == null) {
                // Task has no due date
                return Result.success()
            }
            
            // Check if the task is due soon (within 24 hours)
            if (DateUtils.isDueSoon(dueDate, task.isCompleted)) {
                notificationManager.showTaskReminderNotification(task)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}