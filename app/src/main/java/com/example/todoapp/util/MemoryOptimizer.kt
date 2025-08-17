package com.example.todoapp.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryOptimizer @Inject constructor() {
    
    companion object {
        private const val MEMORY_PRESSURE_THRESHOLD = 0.8f // 80% of available memory
        private const val GC_INTERVAL_MS = 30000L // 30 seconds
    }
    
    /**
     * Check if the device is under memory pressure
     */
    fun isMemoryPressureHigh(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val availableMemory = memoryInfo.availMem
        val totalMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            memoryInfo.totalMem
        } else {
            // Fallback for older versions
            Runtime.getRuntime().maxMemory()
        }
        
        val memoryUsageRatio = (totalMemory - availableMemory).toFloat() / totalMemory.toFloat()
        return memoryUsageRatio > MEMORY_PRESSURE_THRESHOLD
    }
    
    /**
     * Optimize memory usage by clearing caches and running GC
     */
    fun optimizeMemory(context: Context, aggressive: Boolean = false) {
        if (aggressive || isMemoryPressureHigh(context)) {
            // Clear image caches if using image loading libraries
            // Glide.get(context).clearMemory() // Uncomment if using Glide
            
            // Suggest garbage collection
            System.gc()
            
            if (aggressive) {
                // More aggressive cleanup
                Runtime.getRuntime().runFinalization()
                System.gc()
            }
        }
    }
    
    /**
     * Get memory usage statistics
     */
    fun getMemoryStats(context: Context): MemoryStats {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return MemoryStats(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
            freeMemoryMB = runtime.freeMemory() / 1024 / 1024,
            maxMemoryMB = runtime.maxMemory() / 1024 / 1024,
            systemAvailableMemoryMB = memoryInfo.availMem / 1024 / 1024,
            isLowMemory = memoryInfo.lowMemory,
            memoryPressureHigh = isMemoryPressureHigh(context)
        )
    }
    
    /**
     * Monitor memory usage and optimize when needed
     */
    suspend fun startMemoryMonitoring(context: Context) {
        while (true) {
            delay(GC_INTERVAL_MS)
            
            val stats = getMemoryStats(context)
            if (stats.memoryPressureHigh || stats.isLowMemory) {
                optimizeMemory(context, aggressive = stats.isLowMemory)
            }
        }
    }
}

data class MemoryStats(
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
    val maxMemoryMB: Long,
    val systemAvailableMemoryMB: Long,
    val isLowMemory: Boolean,
    val memoryPressureHigh: Boolean
)

/**
 * Composable that automatically manages memory optimization
 */
@Composable
fun AutoMemoryOptimizer(
    memoryOptimizer: MemoryOptimizer,
    enablePeriodicOptimization: Boolean = true
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    // Monitor memory during app lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Optimize memory when app goes to background
                    memoryOptimizer.optimizeMemory(context, aggressive = false)
                }
                Lifecycle.Event.ON_STOP -> {
                    // More aggressive optimization when app is stopped
                    memoryOptimizer.optimizeMemory(context, aggressive = true)
                }
                else -> { /* No action needed */ }
            }
        }
        
        lifecycle.addObserver(observer)
        
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    
    // Periodic memory optimization
    if (enablePeriodicOptimization) {
        LaunchedEffect(Unit) {
            memoryOptimizer.startMemoryMonitoring(context)
        }
    }
}

/**
 * Composable that provides memory-aware behavior
 */
@Composable
fun <T> rememberMemoryAware(
    key: Any?,
    memoryOptimizer: MemoryOptimizer,
    factory: () -> T
): T {
    val context = LocalContext.current
    
    return remember(key) {
        val stats = memoryOptimizer.getMemoryStats(context)
        
        // If memory pressure is high, consider not caching or using simpler alternatives
        if (stats.memoryPressureHigh) {
            // Could return a simpler version or null to avoid caching
            // For now, just log the memory pressure
            android.util.Log.w("MemoryOptimizer", "High memory pressure detected during remember")
        }
        
        factory()
    }
}