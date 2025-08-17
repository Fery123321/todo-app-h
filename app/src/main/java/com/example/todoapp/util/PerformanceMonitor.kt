package com.example.todoapp.util

import android.os.Build
import android.os.Debug
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitor @Inject constructor() {
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val MEMORY_THRESHOLD_MB = 50 // Alert if memory usage exceeds 50MB
        private const val FRAME_TIME_THRESHOLD_MS = 16.67 // 60fps = 16.67ms per frame
    }
    
    var isMonitoring = false
        private set
    
    /**
     * Start monitoring performance metrics
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        Log.d(TAG, "Performance monitoring started")
        logSystemInfo()
    }
    
    /**
     * Stop monitoring performance metrics
     */
    fun stopMonitoring() {
        isMonitoring = false
        Log.d(TAG, "Performance monitoring stopped")
    }
    
    /**
     * Log current memory usage
     */
    fun logMemoryUsage(context: String = "") {
        if (!isMonitoring) return
        
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        
        val memoryInfo = "Memory Usage${if (context.isNotEmpty()) " ($context)" else ""}: " +
                "Used: ${usedMemory}MB, Free: ${freeMemory}MB, Max: ${maxMemory}MB"
        
        if (usedMemory > MEMORY_THRESHOLD_MB) {
            Log.w(TAG, "HIGH $memoryInfo")
        } else {
            Log.d(TAG, memoryInfo)
        }
        
        // Log native heap if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nativeHeapSize = Debug.getNativeHeapSize() / 1024 / 1024
            val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize() / 1024 / 1024
            Log.d(TAG, "Native Heap: Allocated: ${nativeHeapAllocated}MB, Size: ${nativeHeapSize}MB")
        }
    }
    
    /**
     * Measure execution time of a block
     */
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000.0
        
        if (durationMs > FRAME_TIME_THRESHOLD_MS) {
            Log.w(TAG, "SLOW OPERATION: $operation took ${String.format("%.2f", durationMs)}ms")
        } else {
            Log.d(TAG, "Operation: $operation took ${String.format("%.2f", durationMs)}ms")
        }
        
        return result
    }
    
    /**
     * Measure execution time of a suspend block
     */
    suspend inline fun <T> measureSuspendTime(operation: String, block: suspend () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000.0
        
        if (durationMs > FRAME_TIME_THRESHOLD_MS) {
            Log.w(TAG, "SLOW SUSPEND OPERATION: $operation took ${String.format("%.2f", durationMs)}ms")
        } else {
            Log.d(TAG, "Suspend Operation: $operation took ${String.format("%.2f", durationMs)}ms")
        }
        
        return result
    }
    
    /**
     * Log frame rendering performance
     */
    fun logFrameTime(frameTimeNanos: Long) {
        if (!isMonitoring) return
        
        val frameTimeMs = frameTimeNanos / 1_000_000.0
        
        if (frameTimeMs > FRAME_TIME_THRESHOLD_MS) {
            Log.w(TAG, "DROPPED FRAME: Frame took ${String.format("%.2f", frameTimeMs)}ms (target: ${String.format("%.2f", FRAME_TIME_THRESHOLD_MS)}ms)")
        }
    }
    
    /**
     * Log list performance metrics
     */
    fun logListPerformance(
        listName: String,
        itemCount: Int,
        visibleItemCount: Int,
        scrollOffset: Int
    ) {
        if (!isMonitoring) return
        
        Log.d(TAG, "List Performance ($listName): Items: $itemCount, Visible: $visibleItemCount, Scroll: $scrollOffset")
        
        if (itemCount > 1000) {
            Log.w(TAG, "LARGE LIST: $listName has $itemCount items, consider pagination")
        }
    }
    
    /**
     * Force garbage collection and log memory before/after
     */
    fun forceGCAndLogMemory(context: String = "Manual GC") {
        if (!isMonitoring) return
        
        logMemoryUsage("Before $context")
        System.gc()
        // Give GC time to run
        Thread.sleep(100)
        logMemoryUsage("After $context")
    }
    
    private fun logSystemInfo() {
        Log.d(TAG, "System Info:")
        Log.d(TAG, "  Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Log.d(TAG, "  Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        Log.d(TAG, "  Available Processors: ${Runtime.getRuntime().availableProcessors()}")
        
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        Log.d(TAG, "  Max Memory: ${maxMemory}MB")
    }
}

/**
 * Composable that monitors performance during composition
 */
@Composable
fun PerformanceMonitorComposable(
    monitor: PerformanceMonitor,
    screenName: String
) {
    var frameCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(screenName) {
        monitor.logMemoryUsage("Screen: $screenName - Composition Start")
        
        // Monitor memory usage periodically
        while (true) {
            delay(5000) // Check every 5 seconds
            frameCount++
            if (frameCount % 12 == 0) { // Log every minute (12 * 5 seconds)
                monitor.logMemoryUsage("Screen: $screenName - Runtime")
            }
        }
    }
}