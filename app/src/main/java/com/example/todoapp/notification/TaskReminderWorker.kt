package com.example.todoapp.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.domain.repository.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TodoRepository,
    private val notificationManager: TodoNotificationManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val INPUT_TASK_ID = "task_id"
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getString(INPUT_TASK_ID) ?: return Result.failure()
            
            val task = repository.getTaskById(taskId)
            if (task != null && !task.isCompleted) {
                notificationManager.showTaskReminderNotification(task)
            }
            
            Result.success()
        } catch (exception: Exception) {
            android.util.Log.e("TaskReminderWorker", "Failed to send reminder", exception)
            Result.failure()
        }
    }
}