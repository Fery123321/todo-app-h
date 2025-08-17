package com.example.todoapp.data.local

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class DatabaseError : Exception() {
    object CorruptionError : DatabaseError()
    object DiskFullError : DatabaseError()
    object PermissionError : DatabaseError()
    data class ConstraintViolationError(val constraint: String) : DatabaseError()
    data class UnknownError(val cause: Throwable) : DatabaseError()
}

@Singleton
class DatabaseErrorHandler @Inject constructor() {
    
    companion object {
        private const val TAG = "DatabaseErrorHandler"
    }
    
    /**
     * Handle database exceptions and convert them to domain-specific errors
     */
    fun handleDatabaseException(exception: Exception): DatabaseError {
        Log.e(TAG, "Database error occurred", exception)
        
        return when (exception) {
            is SQLiteException -> {
                when {
                    exception.message?.contains("database is locked", ignoreCase = true) == true -> {
                        DatabaseError.PermissionError
                    }
                    exception.message?.contains("disk I/O error", ignoreCase = true) == true -> {
                        DatabaseError.DiskFullError
                    }
                    exception.message?.contains("database disk image is malformed", ignoreCase = true) == true -> {
                        DatabaseError.CorruptionError
                    }
                    exception.message?.contains("UNIQUE constraint failed", ignoreCase = true) == true -> {
                        val constraint = extractConstraintName(exception.message)
                        DatabaseError.ConstraintViolationError(constraint)
                    }
                    exception.message?.contains("FOREIGN KEY constraint failed", ignoreCase = true) == true -> {
                        DatabaseError.ConstraintViolationError("Foreign Key")
                    }
                    else -> DatabaseError.UnknownError(exception)
                }
            }
            else -> DatabaseError.UnknownError(exception)
        }
    }
    
    /**
     * Execute database operation with error handling
     */
    suspend fun <T> safeExecute(operation: suspend () -> T): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val databaseError = handleDatabaseException(e)
            Result.failure(databaseError)
        }
    }
    
    /**
     * Execute database operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        delayMs: Long = 100,
        operation: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Database operation failed, attempt ${attempt + 1}/$maxRetries", e)
                
                // Don't retry for certain types of errors
                val databaseError = handleDatabaseException(e)
                if (databaseError is DatabaseError.CorruptionError || 
                    databaseError is DatabaseError.PermissionError) {
                    return Result.failure(databaseError)
                }
                
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay(delayMs * (attempt + 1))
                }
            }
        }
        
        return Result.failure(handleDatabaseException(lastException!!))
    }
    
    /**
     * Create a callback for Room database that handles corruption
     */
    fun createDatabaseCallback(
        onCorruption: () -> Unit = {}
    ): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCorruption(db: SupportSQLiteDatabase) {
                super.onCorruption(db)
                Log.e(TAG, "Database corruption detected")
                
                // Notify about corruption
                CoroutineScope(Dispatchers.IO).launch {
                    onCorruption()
                }
            }
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.i(TAG, "Database created successfully")
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "Database opened")
                
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON")
                
                // Enable WAL mode for better concurrency
                db.execSQL("PRAGMA journal_mode=WAL")
                
                // Set reasonable timeout for busy database
                db.execSQL("PRAGMA busy_timeout=30000")
            }
        }
    }
    
    private fun extractConstraintName(message: String?): String {
        if (message == null) return "Unknown"
        
        // Try to extract table.column from constraint error message
        val regex = Regex("tasks\\.([a-zA-Z_]+)")
        val match = regex.find(message)
        return match?.groupValues?.get(1) ?: "Unknown"
    }
}