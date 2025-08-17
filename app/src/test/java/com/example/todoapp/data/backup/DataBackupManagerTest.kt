package com.example.todoapp.data.backup

import android.content.Context
import android.net.Uri
import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.data.mapper.TaskMapper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class DataBackupManagerTest {
    
    private lateinit var dataBackupManager: DataBackupManager
    private lateinit var taskDao: TaskDao
    private lateinit var taskMapper: TaskMapper
    private lateinit var context: Context
    
    @Before
    fun setup() {
        taskDao = mockk()
        taskMapper = mockk()
        context = mockk()
        dataBackupManager = DataBackupManager(taskDao, taskMapper)
    }
    
    @Test
    fun `exportData should create valid JSON backup`() = runTest {
        // Given
        val testTasks = listOf(
            TaskEntity(
                id = "1",
                title = "Test Task 1",
                description = "Description 1",
                isCompleted = false,
                priority = "HIGH",
                category = "WORK",
                dueDate = LocalDateTime.of(2024, 1, 1, 12, 0),
                createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
                completedAt = null
            ),
            TaskEntity(
                id = "2",
                title = "Test Task 2",
                description = "Description 2",
                isCompleted = true,
                priority = "LOW",
                category = "PERSONAL",
                dueDate = null,
                createdAt = LocalDateTime.of(2024, 1, 2, 10, 0),
                completedAt = LocalDateTime.of(2024, 1, 2, 15, 0)
            )
        )
        
        val uri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { taskDao.getAllTasks() } returns flowOf(testTasks)
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openOutputStream(uri) } returns outputStream
        
        // When
        val result = dataBackupManager.exportData(context, uri)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("Successfully exported 2 tasks") == true)
        
        val exportedJson = outputStream.toString()
        assertTrue(exportedJson.contains("Test Task 1"))
        assertTrue(exportedJson.contains("Test Task 2"))
        assertTrue(exportedJson.contains("\"version\": 1"))
        
        verify { taskDao.getAllTasks() }
        verify { contentResolver.openOutputStream(uri) }
    }
    
    @Test
    fun `importData should restore tasks from JSON backup`() = runTest {
        // Given
        val backupJson = """
            {
                "version": 1,
                "exportDate": "2024-01-01T10:00:00",
                "tasks": [
                    {
                        "id": "1",
                        "title": "Imported Task",
                        "description": "Imported Description",
                        "isCompleted": false,
                        "priority": "MEDIUM",
                        "category": "WORK",
                        "dueDate": "2024-01-01T12:00:00",
                        "createdAt": "2024-01-01T10:00:00",
                        "completedAt": null
                    }
                ]
            }
        """.trimIndent()
        
        val uri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(backupJson.toByteArray())
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns inputStream
        coEvery { taskDao.insertTasks(any()) } just Runs
        
        // When
        val result = dataBackupManager.importData(context, uri, replaceExisting = false)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("Successfully imported 1 tasks") == true)
        
        verify { contentResolver.openInputStream(uri) }
        coVerify { taskDao.insertTasks(match { it.size == 1 && it[0].title == "Imported Task" }) }
    }
    
    @Test
    fun `importData with replaceExisting should clear existing tasks`() = runTest {
        // Given
        val backupJson = """
            {
                "version": 1,
                "exportDate": "2024-01-01T10:00:00",
                "tasks": []
            }
        """.trimIndent()
        
        val uri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(backupJson.toByteArray())
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns inputStream
        coEvery { taskDao.deleteAllTasks() } just Runs
        coEvery { taskDao.insertTasks(any()) } just Runs
        
        // When
        val result = dataBackupManager.importData(context, uri, replaceExisting = true)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { taskDao.deleteAllTasks() }
        coVerify { taskDao.insertTasks(any()) }
    }
    
    @Test
    fun `createAutoBackup should create backup file in app directory`() = runTest {
        // Given
        val testTasks = listOf(
            TaskEntity(
                id = "1",
                title = "Auto Backup Task",
                description = "Description",
                isCompleted = false,
                priority = "HIGH",
                category = "WORK",
                dueDate = null,
                createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
                completedAt = null
            )
        )
        
        val filesDir = mockk<java.io.File>()
        val backupDir = mockk<java.io.File>()
        val backupFile = mockk<java.io.File>()
        
        every { context.filesDir } returns filesDir
        every { filesDir.resolve("backups") } returns backupDir
        every { backupDir.exists() } returns false
        every { backupDir.mkdirs() } returns true
        every { backupDir.resolve(any<String>()) } returns backupFile
        every { backupDir.listFiles() } returns emptyArray()
        every { backupFile.name } returns "auto_backup_test.json"
        every { backupFile.writeText(any()) } just Runs
        every { taskDao.getAllTasks() } returns flowOf(testTasks)
        
        // When
        val result = dataBackupManager.createAutoBackup(context)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("Auto backup created") == true)
        
        verify { backupDir.mkdirs() }
        verify { backupFile.writeText(any()) }
        verify { taskDao.getAllTasks() }
    }
    
    @Test
    fun `exportData should handle IO errors gracefully`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { taskDao.getAllTasks() } returns flowOf(emptyList())
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openOutputStream(uri) } returns null
        
        // When
        val result = dataBackupManager.exportData(context, uri)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Could not open output stream") == true)
    }
    
    @Test
    fun `importData should handle malformed JSON gracefully`() = runTest {
        // Given
        val malformedJson = "{ invalid json }"
        val uri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(malformedJson.toByteArray())
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns inputStream
        
        // When
        val result = dataBackupManager.importData(context, uri)
        
        // Then
        assertTrue(result.isFailure)
    }
}