package com.example.todoapp.performance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.presentation.components.OptimizedTaskList
import com.example.todoapp.ui.theme.TodoAppTheme
import com.example.todoapp.util.PerformanceMonitor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class UIPerformanceTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testLargeListScrollPerformance() {
        // Given - Large list of tasks
        val largeTasks = generateTestTasks(1000)
        var scrollTime = 0L
        
        composeTestRule.setContent {
            TodoAppTheme {
                OptimizedTaskList(
                    tasks = largeTasks,
                    isLoading = false,
                    onTaskClick = { },
                    onTaskToggle = { },
                    onTaskDelete = { },
                    onTaskEdit = { },
                    performanceMonitor = PerformanceMonitor()
                )
            }
        }
        
        // When - Scroll through the list
        scrollTime = measureTimeMillis {
            composeTestRule.onNodeWithTag("TaskList", useUnmergedTree = true)
                .performScrollToIndex(500)
            
            composeTestRule.onNodeWithTag("TaskList", useUnmergedTree = true)
                .performScrollToIndex(0)
        }
        
        // Then - Scrolling should be smooth (< 1 second for large operations)
        assert(scrollTime < 1000) { "Scroll took ${scrollTime}ms, which is too slow" }
    }
    
    @Test
    fun testListCompositionPerformance() {
        // Given - Medium sized list
        val tasks = generateTestTasks(500)
        var compositionTime = 0L
        
        // When - Measure composition time
        compositionTime = measureTimeMillis {
            composeTestRule.setContent {
                TodoAppTheme {
                    OptimizedTaskList(
                        tasks = tasks,
                        isLoading = false,
                        onTaskClick = { },
                        onTaskToggle = { },
                        onTaskDelete = { },
                        onTaskEdit = { }
                    )
                }
            }
            
            // Wait for composition to complete
            composeTestRule.waitForIdle()
        }
        
        // Then - Composition should be fast (< 500ms for 500 items)
        assert(compositionTime < 500) { "Composition took ${compositionTime}ms, which is too slow" }
    }
    
    @Test
    fun testTaskItemInteractionPerformance() {
        // Given - Single task
        val task = generateTestTasks(1).first()
        var interactionTime = 0L
        
        composeTestRule.setContent {
            TodoAppTheme {
                OptimizedTaskList(
                    tasks = listOf(task),
                    isLoading = false,
                    onTaskClick = { },
                    onTaskToggle = { },
                    onTaskDelete = { },
                    onTaskEdit = { }
                )
            }
        }
        
        // When - Interact with task item
        interactionTime = measureTimeMillis {
            // Find and click the task checkbox
            composeTestRule.onNodeWithContentDescription("Toggle task completion")
                .performClick()
            
            composeTestRule.waitForIdle()
        }
        
        // Then - Interaction should be immediate (< 100ms)
        assert(interactionTime < 100) { "Task interaction took ${interactionTime}ms, which is too slow" }
    }
    
    @Test
    fun testListUpdatePerformance() {
        // Given - Initial list
        var tasks = generateTestTasks(100)
        var updateTime = 0L
        
        composeTestRule.setContent {
            TodoAppTheme {
                OptimizedTaskList(
                    tasks = tasks,
                    isLoading = false,
                    onTaskClick = { },
                    onTaskToggle = { },
                    onTaskDelete = { },
                    onTaskEdit = { }
                )
            }
        }
        
        composeTestRule.waitForIdle()
        
        // When - Update the list
        updateTime = measureTimeMillis {
            tasks = tasks + generateTestTasks(50, startIndex = 101)
            
            composeTestRule.setContent {
                TodoAppTheme {
                    OptimizedTaskList(
                        tasks = tasks,
                        isLoading = false,
                        onTaskClick = { },
                        onTaskToggle = { },
                        onTaskDelete = { },
                        onTaskEdit = { }
                    )
                }
            }
            
            composeTestRule.waitForIdle()
        }
        
        // Then - List update should be fast (< 200ms)
        assert(updateTime < 200) { "List update took ${updateTime}ms, which is too slow" }
    }
    
    @Test
    fun testMemoryUsageDuringScrolling() {
        // Given - Large list
        val largeTasks = generateTestTasks(2000)
        
        composeTestRule.setContent {
            TodoAppTheme {
                OptimizedTaskList(
                    tasks = largeTasks,
                    isLoading = false,
                    onTaskClick = { },
                    onTaskToggle = { },
                    onTaskDelete = { },
                    onTaskEdit = { }
                )
            }
        }
        
        // When - Measure memory before and after scrolling
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Scroll through the entire list
        repeat(10) { index ->
            composeTestRule.onNodeWithTag("TaskList", useUnmergedTree = true)
                .performScrollToIndex(index * 200)
            composeTestRule.waitForIdle()
        }
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100)
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = (finalMemory - initialMemory) / 1024 / 1024 // Convert to MB
        
        // Then - Memory increase should be reasonable (< 50MB)
        assert(memoryIncrease < 50) { "Memory increased by ${memoryIncrease}MB during scrolling" }
    }
    
    private fun generateTestTasks(count: Int, startIndex: Int = 1): List<TodoTask> {
        val categories = Category.values()
        val priorities = Priority.values()
        
        return (startIndex until startIndex + count).map { index ->
            TodoTask(
                id = "task_$index",
                title = "Performance Test Task $index",
                description = "Description for performance test task $index with some content to test rendering",
                isCompleted = index % 4 == 0,
                priority = priorities[index % priorities.size],
                category = categories[index % categories.size],
                dueDate = if (index % 7 == 0) LocalDateTime.now().plusDays(index.toLong()) else null,
                createdAt = LocalDateTime.now().minusDays(index.toLong()),
                completedAt = if (index % 4 == 0) LocalDateTime.now() else null
            )
        }
    }
}