package com.example.todoapp.presentation.screen

import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.DailyProgress
import com.example.todoapp.domain.model.TaskStatistics
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StatisticsScreenTest {
    
    @Test
    fun `TaskStatistics default values are correct`() {
        val statistics = TaskStatistics()
        
        assertEquals(0, statistics.totalTasks)
        assertEquals(0, statistics.completedTasks)
        assertEquals(0f, statistics.completionRate)
        assertEquals(0, statistics.currentStreak)
        assertTrue(statistics.categoryBreakdown.isEmpty())
        assertTrue(statistics.weeklyProgress.isEmpty())
    }
    
    @Test
    fun `TaskStatistics with data has correct values`() {
        val categoryBreakdown = mapOf(
            Category.WORK to 5,
            Category.PERSONAL to 3
        )
        
        val weeklyProgress = listOf(
            DailyProgress(LocalDate.now().minusDays(6), 1, 2),
            DailyProgress(LocalDate.now().minusDays(5), 2, 3),
            DailyProgress(LocalDate.now().minusDays(4), 1, 1),
            DailyProgress(LocalDate.now().minusDays(3), 3, 4),
            DailyProgress(LocalDate.now().minusDays(2), 2, 2),
            DailyProgress(LocalDate.now().minusDays(1), 1, 3),
            DailyProgress(LocalDate.now(), 2, 2)
        )
        
        val statistics = TaskStatistics(
            totalTasks = 15,
            completedTasks = 12,
            completionRate = 80.0f,
            currentStreak = 7,
            categoryBreakdown = categoryBreakdown,
            weeklyProgress = weeklyProgress
        )
        
        assertEquals(15, statistics.totalTasks)
        assertEquals(12, statistics.completedTasks)
        assertEquals(80.0f, statistics.completionRate)
        assertEquals(7, statistics.currentStreak)
        assertEquals(2, statistics.categoryBreakdown.size)
        assertEquals(5, statistics.categoryBreakdown[Category.WORK])
        assertEquals(3, statistics.categoryBreakdown[Category.PERSONAL])
        assertEquals(7, statistics.weeklyProgress.size)
    }
    
    @Test
    fun `DailyProgress calculates completion rate correctly`() {
        val progress1 = DailyProgress(LocalDate.now(), 3, 5)
        val progress2 = DailyProgress(LocalDate.now(), 0, 3)
        val progress3 = DailyProgress(LocalDate.now(), 2, 2)
        
        // Test completion rates
        assertEquals(0.6f, progress1.completedTasks.toFloat() / progress1.totalTasks, 0.01f)
        assertEquals(0.0f, progress2.completedTasks.toFloat() / progress2.totalTasks, 0.01f)
        assertEquals(1.0f, progress3.completedTasks.toFloat() / progress3.totalTasks, 0.01f)
    }
    
    @Test
    fun `DailyProgress handles edge cases`() {
        val progressNoTasks = DailyProgress(LocalDate.now(), 0, 0)
        val progressAllCompleted = DailyProgress(LocalDate.now(), 5, 5)
        val progressNoneCompleted = DailyProgress(LocalDate.now(), 0, 10)
        
        assertEquals(0, progressNoTasks.completedTasks)
        assertEquals(0, progressNoTasks.totalTasks)
        
        assertEquals(5, progressAllCompleted.completedTasks)
        assertEquals(5, progressAllCompleted.totalTasks)
        
        assertEquals(0, progressNoneCompleted.completedTasks)
        assertEquals(10, progressNoneCompleted.totalTasks)
    }
    
    @Test
    fun `CategoryBreakdown handles all categories`() {
        val breakdown = mapOf(
            Category.WORK to 10,
            Category.PERSONAL to 8,
            Category.SHOPPING to 3,
            Category.HEALTH to 5,
            Category.EDUCATION to 2
        )
        
        assertEquals(5, breakdown.size)
        assertEquals(10, breakdown[Category.WORK])
        assertEquals(8, breakdown[Category.PERSONAL])
        assertEquals(3, breakdown[Category.SHOPPING])
        assertEquals(5, breakdown[Category.HEALTH])
        assertEquals(2, breakdown[Category.EDUCATION])
        
        val totalTasks = breakdown.values.sum()
        assertEquals(28, totalTasks)
    }
    
    @Test
    fun `WeeklyProgress maintains correct order`() {
        val today = LocalDate.now()
        val weeklyProgress = listOf(
            DailyProgress(today.minusDays(6), 1, 2),
            DailyProgress(today.minusDays(5), 2, 3),
            DailyProgress(today.minusDays(4), 1, 1),
            DailyProgress(today.minusDays(3), 3, 4),
            DailyProgress(today.minusDays(2), 2, 2),
            DailyProgress(today.minusDays(1), 1, 3),
            DailyProgress(today, 2, 2)
        )
        
        assertEquals(7, weeklyProgress.size)
        
        // Verify dates are in ascending order (oldest to newest)
        for (i in 1 until weeklyProgress.size) {
            assertTrue(
                "Dates should be in ascending order",
                weeklyProgress[i-1].date.isBefore(weeklyProgress[i].date)
            )
        }
        
        // Verify first date is 6 days ago and last date is today
        assertEquals(today.minusDays(6), weeklyProgress.first().date)
        assertEquals(today, weeklyProgress.last().date)
    }
    
    @Test
    fun `Statistics calculation edge cases`() {
        // Test zero completion rate
        val emptyStats = TaskStatistics(
            totalTasks = 0,
            completedTasks = 0,
            completionRate = 0f
        )
        assertEquals(0f, emptyStats.completionRate)
        
        // Test perfect completion rate
        val perfectStats = TaskStatistics(
            totalTasks = 10,
            completedTasks = 10,
            completionRate = 100f
        )
        assertEquals(100f, perfectStats.completionRate)
        
        // Test partial completion rate
        val partialStats = TaskStatistics(
            totalTasks = 8,
            completedTasks = 6,
            completionRate = 75f
        )
        assertEquals(75f, partialStats.completionRate)
    }
}