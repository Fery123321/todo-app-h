package com.example.todoapp.di

import com.example.todoapp.util.MemoryOptimizer
import com.example.todoapp.util.PerformanceMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }
    
    @Provides
    @Singleton
    fun provideMemoryOptimizer(): MemoryOptimizer {
        return MemoryOptimizer()
    }
}