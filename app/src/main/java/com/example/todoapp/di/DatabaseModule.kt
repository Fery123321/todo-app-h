package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            TodoDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideTaskDao(database: TodoDatabase): TaskDao {
        return database.taskDao()
    }
}