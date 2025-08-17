package com.example.todoapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID = "todo_reminders"
        const val CHANNEL_NAME = "Task Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for task due date reminders"
        
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for task reminders
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows a task reminder notification
     */
    fun showTaskReminderNotification(task: TodoTask) {
        if (!hasNotificationPermission()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Reminder")
            .setContentText(task.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildNotificationText(task))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(createMarkCompleteAction(task))
            .build()
        
        val notificationId = NOTIFICATION_ID_BASE + task.id.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    /**
     * Creates the "Mark Complete" action for notifications
     */
    private fun createMarkCompleteAction(task: TodoTask): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, task.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            0, // No icon for now
            "Mark Complete",
            pendingIntent
        ).build()
    }
    
    /**
     * Builds the notification text with task details
     */
    private fun buildNotificationText(task: TodoTask): String {
        val builder = StringBuilder()
        
        if (task.description.isNotBlank()) {
            builder.append(task.description)
            builder.append("\n\n")
        }
        
        task.dueDate?.let { dueDate ->
            val dueDateText = when {
                DateUtils.isOverdue(dueDate, task.isCompleted) -> "Overdue: ${DateUtils.formatDateTime(dueDate)}"
                DateUtils.isDueToday(dueDate, task.isCompleted) -> "Due today: ${DateUtils.formatTime(dueDate)}"
                else -> "Due: ${DateUtils.formatDateTime(dueDate)}"
            }
            builder.append(dueDateText)
        }
        
        return builder.toString().trim()
    }
    
    /**
     * Cancels a notification for a specific task
     */
    fun cancelTaskNotification(taskId: String) {
        val notificationId = NOTIFICATION_ID_BASE + taskId.hashCode()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Checks if the app has notification permission
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    /**
     * Checks if notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}