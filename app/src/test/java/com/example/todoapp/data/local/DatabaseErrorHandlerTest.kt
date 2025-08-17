package com.example.todoapp.data.local

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DatabaseErrorHandlerTest {
    
    private lateinit var errorHandler: DatabaseErrorHandler
    
    @Before
    fun setup() {
        errorHandler = DatabaseErrorHandler()
    }
    
    @Test
    fun `handleDatabaseException should return CorruptionError for malformed database`() {
        // Given
        val exception = SQLiteException("database disk image is malformed")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.CorruptionError)
    }
    
    @Test
    fun `handleDatabaseException should return DiskFullError for disk IO error`() {
        // Given
        val exception = SQLiteException("disk I/O error")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.DiskFullError)
    }
    
    @Test
    fun `handleDatabaseException should return PermissionError for locked database`() {
        // Given
        val exception = SQLiteException("database is locked")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.PermissionError)
    }
    
    @Test
    fun `handleDatabaseException should return ConstraintViolationError for unique constraint`() {
        // Given
        val exception = SQLiteException("UNIQUE constraint failed: tasks.id")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.ConstraintViolationError)
        assertEquals("id", (result as DatabaseError.ConstraintViolationError).constraint)
    }
    
    @Test
    fun `handleDatabaseException should return ConstraintViolationError for foreign key constraint`() {
        // Given
        val exception = SQLiteException("FOREIGN KEY constraint failed")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.ConstraintViolationError)
        assertEquals("Foreign Key", (result as DatabaseError.ConstraintViolationError).constraint)
    }
    
    @Test
    fun `handleDatabaseException should return UnknownError for other exceptions`() {
        // Given
        val exception = RuntimeException("Some other error")
        
        // When
        val result = errorHandler.handleDatabaseException(exception)
        
        // Then
        assertTrue(result is DatabaseError.UnknownError)
        assertEquals(exception, (result as DatabaseError.UnknownError).cause)
    }
    
    @Test
    fun `safeExecute should return success for successful operation`() = runTest {
        // Given
        val operation: suspend () -> String = { "success" }
        
        // When
        val result = errorHandler.safeExecute(operation)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }
    
    @Test
    fun `safeExecute should return failure for failed operation`() = runTest {
        // Given
        val exception = SQLiteException("database error")
        val operation: suspend () -> String = { throw exception }
        
        // When
        val result = errorHandler.safeExecute(operation)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DatabaseError)
    }
    
    @Test
    fun `executeWithRetry should retry on transient errors`() = runTest {
        // Given
        var attemptCount = 0
        val operation: suspend () -> String = {
            attemptCount++
            if (attemptCount < 3) {
                throw SQLiteException("temporary error")
            }
            "success"
        }
        
        // When
        val result = errorHandler.executeWithRetry(maxRetries = 3, delayMs = 1, operation)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should not retry on corruption errors`() = runTest {
        // Given
        var attemptCount = 0
        val operation: suspend () -> String = {
            attemptCount++
            throw SQLiteException("database disk image is malformed")
        }
        
        // When
        val result = errorHandler.executeWithRetry(maxRetries = 3, delayMs = 1, operation)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DatabaseError.CorruptionError)
        assertEquals(1, attemptCount) // Should not retry
    }
    
    @Test
    fun `executeWithRetry should fail after max retries`() = runTest {
        // Given
        var attemptCount = 0
        val operation: suspend () -> String = {
            attemptCount++
            throw SQLiteException("persistent error")
        }
        
        // When
        val result = errorHandler.executeWithRetry(maxRetries = 2, delayMs = 1, operation)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(2, attemptCount)
    }
}