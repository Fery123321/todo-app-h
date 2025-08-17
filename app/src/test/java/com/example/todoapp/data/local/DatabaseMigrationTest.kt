package com.example.todoapp.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO tasks (id, title, description, isCompleted, priority, category, dueDate, createdAt, completedAt) VALUES ('1', 'Test Task', 'Description', 0, 'HIGH', 'WORK', NULL, 1640995200000, NULL)")
            
            // Prepare for the next version.
            close()
        }
        
        // Re-open the database with version 2 and provide MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)
        
        // MigrationTestHelper automatically verifies the schema changes,
        // but you can also validate that the data was migrated properly.
        val cursor = db.query("SELECT * FROM tasks WHERE id = '1'")
        assert(cursor.count == 1)
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            // Insert test data
            execSQL("INSERT INTO tasks (id, title, description, isCompleted, priority, category, dueDate, createdAt, completedAt) VALUES ('1', 'Test Task', 'Description', 0, 'HIGH', 'WORK', NULL, 1640995200000, NULL)")
            close()
        }
        
        // Migrate to version 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, TodoDatabase.MIGRATION_2_3)
        
        // Verify indices were created
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_tasks_%'")
        assert(cursor.count >= 4) // Should have at least 4 indices
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create the database with version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data
            execSQL("INSERT INTO tasks (id, title, description, isCompleted, priority, category, dueDate, createdAt, completedAt) VALUES ('1', 'Test Task', 'Description', 0, 'HIGH', 'WORK', NULL, 1640995200000, NULL)")
            close()
        }
        
        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TodoDatabase::class.java,
            TEST_DB
        ).addMigrations(TodoDatabase.MIGRATION_1_2, TodoDatabase.MIGRATION_2_3).build().apply {
            openHelper.writableDatabase.close()
        }
    }
}