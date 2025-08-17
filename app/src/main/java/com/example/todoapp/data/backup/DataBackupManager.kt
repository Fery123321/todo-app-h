package com.example.todoapp.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.data.mapper.toDomainModel
import com.example.todoapp.data.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val tasks: List<SerializableTask>
)

@Serializable
data class SerializableTask(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val category: String,
    val dueDate: String?,
    val createdAt: String,
    val completedAt: String?
)

@Singleton
class DataBackupManager @Inject constructor(
    private val taskDao: TaskDao
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Export all tasks to a JSON backup file
     */
    suspend fun exportData(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getAllTasks().first()
            
            val serializableTasks = tasks.map { entity ->
                SerializableTask(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    isCompleted = entity.isCompleted,
                    priority = entity.priority,
                    category = entity.category,
                    dueDate = entity.dueDate?.toString(),
                    createdAt = entity.createdAt.toString(),
                    completedAt = entity.completedAt?.toString()
                )
            }
            
            val backupData = BackupData(
                exportDate = LocalDateTime.now().toString(),
                tasks = serializableTasks
            )
            
            val jsonString = json.encodeToString(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: throw IOException("Could not open output stream")
            
            Result.success("Successfully exported ${tasks.size} tasks")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import tasks from a JSON backup file
     */
    suspend fun importData(context: Context, uri: Uri, replaceExisting: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: throw IOException("Could not read backup file")
            
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            if (replaceExisting) {
                taskDao.deleteAllTasks()
            }
            
            val taskEntities = backupData.tasks.mapNotNull { serializableTask ->
                try {
                    TaskEntity(
                        id = serializableTask.id,
                        title = serializableTask.title,
                        description = serializableTask.description,
                        isCompleted = serializableTask.isCompleted,
                        priority = serializableTask.priority,
                        category = serializableTask.category,
                        dueDate = serializableTask.dueDate?.let { LocalDateTime.parse(it) },
                        createdAt = LocalDateTime.parse(serializableTask.createdAt),
                        completedAt = serializableTask.completedAt?.let { LocalDateTime.parse(it) }
                    )
                } catch (e: Exception) {
                    // Skip invalid tasks but continue with others
                    null
                }
            }
            
            taskDao.insertTasks(taskEntities)
            
            Result.success("Successfully imported ${taskEntities.size} tasks")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create automatic backup to app's private storage
     */
    suspend fun createAutoBackup(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupDir = context.filesDir.resolve("backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val timestamp = LocalDateTime.now().toString().replace(":", "-")
            val backupFile = backupDir.resolve("auto_backup_$timestamp.json")
            
            val tasks = taskDao.getAllTasks().first()
            
            val serializableTasks = tasks.map { entity ->
                SerializableTask(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    isCompleted = entity.isCompleted,
                    priority = entity.priority,
                    category = entity.category,
                    dueDate = entity.dueDate?.toString(),
                    createdAt = entity.createdAt.toString(),
                    completedAt = entity.completedAt?.toString()
                )
            }
            
            val backupData = BackupData(
                exportDate = LocalDateTime.now().toString(),
                tasks = serializableTasks
            )
            
            val jsonString = json.encodeToString(backupData)
            backupFile.writeText(jsonString)
            
            // Clean up old backups (keep only last 5)
            cleanupOldBackups(backupDir)
            
            Result.success("Auto backup created: ${backupFile.name}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restore from the most recent automatic backup
     */
    suspend fun restoreFromAutoBackup(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupDir = context.filesDir.resolve("backups")
            val backupFiles = backupDir.listFiles()?.filter { it.name.startsWith("auto_backup_") }
                ?.sortedByDescending { it.lastModified() }
            
            val latestBackup = backupFiles?.firstOrNull()
                ?: return@withContext Result.failure(Exception("No automatic backups found"))
            
            val jsonString = latestBackup.readText()
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            taskDao.deleteAllTasks()
            
            val taskEntities = backupData.tasks.mapNotNull { serializableTask ->
                try {
                    TaskEntity(
                        id = serializableTask.id,
                        title = serializableTask.title,
                        description = serializableTask.description,
                        isCompleted = serializableTask.isCompleted,
                        priority = serializableTask.priority,
                        category = serializableTask.category,
                        dueDate = serializableTask.dueDate?.let { LocalDateTime.parse(it) },
                        createdAt = LocalDateTime.parse(serializableTask.createdAt),
                        completedAt = serializableTask.completedAt?.let { LocalDateTime.parse(it) }
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            taskDao.insertTasks(taskEntities)
            
            Result.success("Restored ${taskEntities.size} tasks from backup: ${latestBackup.name}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun cleanupOldBackups(backupDir: java.io.File) {
        val backupFiles = backupDir.listFiles()?.filter { it.name.startsWith("auto_backup_") }
            ?.sortedByDescending { it.lastModified() }
        
        // Keep only the 5 most recent backups
        backupFiles?.drop(5)?.forEach { it.delete() }
    }
}