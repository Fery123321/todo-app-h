package com.example.todoapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    
    companion object {
        const val DATABASE_NAME = "todo_database"
        
        // Migration from version 1 to 2 - example migration for future schema changes
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add a new column for task tags
                // database.execSQL("ALTER TABLE tasks ADD COLUMN tags TEXT")
                
                // For now, this is a placeholder migration that doesn't change anything
                // but demonstrates the migration structure
            }
        }
        
        // Migration from version 2 to 3 - example for adding indices
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_category ON tasks(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_priority ON tasks(priority)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueDate ON tasks(dueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isCompleted ON tasks(isCompleted)")
            }
        }
        
        fun buildDatabase(
            context: Context,
            errorHandler: DatabaseErrorHandler
        ): TodoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(errorHandler.createDatabaseCallback())
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}