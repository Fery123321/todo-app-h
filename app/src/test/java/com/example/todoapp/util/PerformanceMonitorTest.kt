package com.example.todoapp.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PerformanceMonitorTest {
    
    private lateinit var performanceMonitor: PerformanceMonitor
    
    @Before
    fun setup() {
        performanceMonitor = PerformanceMonitor()
    }
    
    @Test
    fun `measureTime should return correct result and measure execution time`() {
        // Given
        val expectedResult = "test result"
        val operation = "test operation"
        
        // When
        val result = performanceMonitor.measureTime(operation) {
            Thread.sleep(10) // Small delay to ensure measurable time
            expectedResult
        }
        
        // Then
        assertEquals(expectedResult, result)
    }
    
    @Test
    fun `measureSuspendTime should return correct result and measure execution time`() = runTest {
        // Given
        val expectedResult = "suspend test result"
        val operation = "suspend test operation"
        
        // When
        val result = performanceMonitor.measureSuspendTime(operation) {
            delay(10) // Small delay to ensure measurable time
            expectedResult
        }
        
        // Then
        assertEquals(expectedResult, result)
    }
    
    @Test
    fun `startMonitoring should set monitoring flag`() {
        // Given
        assertFalse(performanceMonitor.isMonitoring)
        
        // When
        performanceMonitor.startMonitoring()
        
        // Then
        assertTrue(performanceMonitor.isMonitoring)
    }
    
    @Test
    fun `stopMonitoring should unset monitoring flag`() {
        // Given
        performanceMonitor.startMonitoring()
        assertTrue(performanceMonitor.isMonitoring)
        
        // When
        performanceMonitor.stopMonitoring()
        
        // Then
        assertFalse(performanceMonitor.isMonitoring)
    }
    
    @Test
    fun `logMemoryUsage should not throw exception`() {
        // Given
        performanceMonitor.startMonitoring()
        
        // When & Then - Should not throw
        performanceMonitor.logMemoryUsage("test context")
        performanceMonitor.logMemoryUsage() // Without context
    }
    
    @Test
    fun `logListPerformance should not throw exception`() {
        // Given
        performanceMonitor.startMonitoring()
        
        // When & Then - Should not throw
        performanceMonitor.logListPerformance(
            listName = "TestList",
            itemCount = 100,
            visibleItemCount = 10,
            scrollOffset = 50
        )
    }
    
    @Test
    fun `forceGCAndLogMemory should not throw exception`() {
        // Given
        performanceMonitor.startMonitoring()
        
        // When & Then - Should not throw
        performanceMonitor.forceGCAndLogMemory("test GC")
    }
    
    @Test
    fun `logFrameTime should not throw exception`() {
        // Given
        performanceMonitor.startMonitoring()
        val frameTimeNanos = 16_000_000L // 16ms in nanoseconds
        
        // When & Then - Should not throw
        performanceMonitor.logFrameTime(frameTimeNanos)
    }
}