package com.example.todoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.domain.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var todoRepository: TodoRepository
    
    @Inject
    lateinit var notificationManager: TodoNotificationManager
    
    companion object {
        const val ACTION_MARK_COMPLETE = "com.example.todoapp.ACTION_MARK_COMPLETE"
        const val EXTRA_TASK_ID = "task_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_COMPLETE -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                if (taskId != null) {
                    markTaskComplete(taskId)
                }
            }
        }
    }
    
    private fun markTaskComplete(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                todoRepository.toggleTaskCompletion(taskId)
                notificationManager.cancelTaskNotification(taskId)
            } catch (e: Exception) {
                // Log error or handle gracefully
                e.printStackTrace()
            }
        }
    }
}