package com.example.todoapp.util

import android.app.ActivityManager
import android.content.Context
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MemoryOptimizerTest {
    
    private lateinit var memoryOptimizer: MemoryOptimizer
    private lateinit var context: Context
    private lateinit var activityManager: ActivityManager
    
    @Before
    fun setup() {
        memoryOptimizer = MemoryOptimizer()
        context = mockk()
        activityManager = mockk()
        
        every { context.getSystemService(Context.ACTIVITY_SERVICE) } returns activityManager
    }
    
    @Test
    fun `isMemoryPressureHigh should return true when memory usage is high`() {
        // Given
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 200_000_000L // 200MB available
            totalMem = 1_000_000_000L // 1GB total
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
        }
        
        // When
        val result = memoryOptimizer.isMemoryPressureHigh(context)
        
        // Then
        assertTrue("Memory pressure should be high when 80% of memory is used", result)
    }
    
    @Test
    fun `isMemoryPressureHigh should return false when memory usage is low`() {
        // Given
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 700_000_000L // 700MB available
            totalMem = 1_000_000_000L // 1GB total
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
        }
        
        // When
        val result = memoryOptimizer.isMemoryPressureHigh(context)
        
        // Then
        assertFalse("Memory pressure should be low when only 30% of memory is used", result)
    }
    
    @Test
    fun `optimizeMemory should not throw exception`() {
        // Given
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 200_000_000L
            totalMem = 1_000_000_000L
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
        }
        
        // When & Then - Should not throw
        memoryOptimizer.optimizeMemory(context, aggressive = false)
        memoryOptimizer.optimizeMemory(context, aggressive = true)
    }
    
    @Test
    fun `getMemoryStats should return valid memory statistics`() {
        // Given
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 500_000_000L // 500MB available
            totalMem = 1_000_000_000L // 1GB total
            lowMemory = false
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
            info.lowMemory = memoryInfo.lowMemory
        }
        
        // When
        val stats = memoryOptimizer.getMemoryStats(context)
        
        // Then
        assertTrue("Used memory should be positive", stats.usedMemoryMB > 0)
        assertTrue("Free memory should be positive", stats.freeMemoryMB > 0)
        assertTrue("Max memory should be positive", stats.maxMemoryMB > 0)
        assertTrue("System available memory should be positive", stats.systemAvailableMemoryMB > 0)
        assertFalse("Low memory flag should match", stats.isLowMemory)
        assertFalse("Memory pressure should be low", stats.memoryPressureHigh)
    }
    
    @Test
    fun `getMemoryStats should detect low memory condition`() {
        // Given
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 100_000_000L // 100MB available
            totalMem = 1_000_000_000L // 1GB total
            lowMemory = true
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
            info.lowMemory = memoryInfo.lowMemory
        }
        
        // When
        val stats = memoryOptimizer.getMemoryStats(context)
        
        // Then
        assertTrue("Low memory flag should be true", stats.isLowMemory)
        assertTrue("Memory pressure should be high", stats.memoryPressureHigh)
    }
}