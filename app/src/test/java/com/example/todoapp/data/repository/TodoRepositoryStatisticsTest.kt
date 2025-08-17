package com.example.todoapp.data.repository

import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.TaskEntity
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.DailyProgress
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TaskStatistics
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class TodoRepositoryStatisticsTest {
    
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TodoRepositoryImpl
    
    @Before
    fun setup() {
        taskDao = mockk()
        repository = TodoRepositoryImpl(taskDao)
    }
    
    @Test
    fun `getCompletionRate returns correct percentage when tasks exist`() = runTest {
        // Given
        coEvery { taskDao.getTotalTaskCount() } returns 10
        coEvery { taskDao.getCompletedTaskCount() } returns 7
        
        // When
        val completionRate = repository.getCompletionRate()
        
        // Then
        assertEquals(70.0f, completionRate, 0.01f)
    }
    
    @Test
    fun `getCompletionRate returns zero when no tasks exist`() = runTest {
        // Given
        coEvery { taskDao.getTotalTaskCount() } returns 0
        coEvery { taskDao.getCompletedTaskCount() } returns 0
        
        // When
        val completionRate = repository.getCompletionRate()
        
        // Then
        assertEquals(0.0f, completionRate)
    }
    
    @Test
    fun `getCompletionRate returns zero when no completed tasks exist`() = runTest {
        // Given
        coEvery { taskDao.getTotalTaskCount() } returns 5
        coEvery { taskDao.getCompletedTaskCount() } returns 0
        
        // When
        val completionRate = repository.getCompletionRate()
        
        // Then
        assertEquals(0.0f, completionRate)
    }
    
    @Test
    fun `getCategoryBreakdown returns correct distribution`() = runTest {
        // Given
        coEvery { taskDao.getTaskCountByCategory("WORK") } returns 5
        coEvery { taskDao.getTaskCountByCategory("PERSONAL") } returns 3
        coEvery { taskDao.getTaskCountByCategory("SHOPPING") } returns 0
        coEvery { taskDao.getTaskCountByCategory("HEALTH") } returns 2
        coEvery { taskDao.getTaskCountByCategory("EDUCATION") } returns 0
        
        // When
        val breakdown = repository.getCategoryBreakdown()
        
        // Then
        assertEquals(3, breakdown.size)
        assertEquals(5, breakdown[Category.WORK])
        assertEquals(3, breakdown[Category.PERSONAL])
        assertEquals(2, breakdown[Category.HEALTH])
        assertFalse(breakdown.containsKey(Category.SHOPPING))
        assertFalse(breakdown.containsKey(Category.EDUCATION))
    }
    
    @Test
    fun `getCategoryBreakdown returns empty map when no tasks exist`() = runTest {
        // Given
        Category.values().forEach { category ->
            coEvery { taskDao.getTaskCountByCategory(category.name) } returns 0
        }
        
        // When
        val breakdown = repository.getCategoryBreakdown()
        
        // Then
        assertTrue(breakdown.isEmpty())
    }
    
    @Test
    fun `getDailyProgress returns correct progress for specific date`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfDay = date.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay) 
        } returns 3
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay) 
        } returns 5
        
        // When
        val dailyProgress = repository.getDailyProgress(date)
        
        // Then
        assertEquals(date, dailyProgress.date)
        assertEquals(3, dailyProgress.completedTasks)
        assertEquals(5, dailyProgress.totalTasks)
    }
    
    @Test
    fun `getWeeklyProgress returns 7 days of progress`() = runTest {
        // Given
        val today = LocalDate.now()
        
        // Mock daily progress for each day in the week
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val endOfDay = date.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
            
            // The expected values should match what we're testing for
            val dayIndex = 6 - i // Convert to 0-6 index where 0 is oldest day
            coEvery { 
                taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay) 
            } returns dayIndex + 1
            coEvery { 
                taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay) 
            } returns (dayIndex + 1) * 2
        }
        
        // When
        val weeklyProgress = repository.getWeeklyProgress()
        
        // Then
        assertEquals(7, weeklyProgress.size)
        
        // Verify the dates are in correct order (oldest to newest)
        for (i in 0 until 7) {
            val expectedDate = today.minusDays((6 - i).toLong())
            assertEquals(expectedDate, weeklyProgress[i].date)
            assertEquals(i + 1, weeklyProgress[i].completedTasks)
            assertEquals((i + 1) * 2, weeklyProgress[i].totalTasks)
        }
    }
    
    @Test
    fun `getCurrentStreak returns correct streak count`() = runTest {
        // Given
        val today = LocalDate.now()
        
        // Mock 3 consecutive days with completed tasks
        for (i in 0 until 3) {
            val date = today.minusDays(i.toLong())
            val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val endOfDay = date.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
            
            coEvery { 
                taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay) 
            } returns 2
            coEvery { 
                taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay) 
            } returns 3
        }
        
        // Mock day 4 with no completed tasks (breaks streak)
        val date4 = today.minusDays(3)
        val startOfDay4 = date4.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfDay4 = date4.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfDay4, endOfDay4) 
        } returns 0
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfDay4, endOfDay4) 
        } returns 2
        
        // When
        val streak = repository.getCurrentStreak()
        
        // Then
        assertEquals(3, streak)
    }
    
    @Test
    fun `getCurrentStreak returns zero when no tasks completed today`() = runTest {
        // Given
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfDay = today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay) 
        } returns 0
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay) 
        } returns 3
        
        // When
        val streak = repository.getCurrentStreak()
        
        // Then
        assertEquals(0, streak)
    }
    
    @Test
    fun `getCurrentStreak handles today with no tasks created`() = runTest {
        // Given
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        // Today: no tasks created
        val startOfToday = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfToday = today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfToday, endOfToday) 
        } returns 0
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfToday, endOfToday) 
        } returns 0
        
        // Yesterday: tasks completed
        val startOfYesterday = yesterday.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfYesterday = yesterday.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfYesterday, endOfYesterday) 
        } returns 2
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfYesterday, endOfYesterday) 
        } returns 3
        
        // Day before yesterday: no completed tasks (breaks streak)
        val dayBefore = yesterday.minusDays(1)
        val startOfDayBefore = dayBefore.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endOfDayBefore = dayBefore.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
        
        coEvery { 
            taskDao.getCompletedTaskCountInDateRange(startOfDayBefore, endOfDayBefore) 
        } returns 0
        coEvery { 
            taskDao.getTotalTaskCountInDateRange(startOfDayBefore, endOfDayBefore) 
        } returns 2
        
        // When
        val streak = repository.getCurrentStreak()
        
        // Then
        assertEquals(1, streak) // Should continue streak from yesterday
    }
    
    @Test
    fun `getTaskStatistics returns complete statistics object`() = runTest {
        // Given
        coEvery { taskDao.getTotalTaskCount() } returns 15
        coEvery { taskDao.getCompletedTaskCount() } returns 10
        
        // Mock category breakdown
        coEvery { taskDao.getTaskCountByCategory("WORK") } returns 8
        coEvery { taskDao.getTaskCountByCategory("PERSONAL") } returns 5
        coEvery { taskDao.getTaskCountByCategory("SHOPPING") } returns 2
        coEvery { taskDao.getTaskCountByCategory("HEALTH") } returns 0
        coEvery { taskDao.getTaskCountByCategory("EDUCATION") } returns 0
        
        // Mock weekly progress
        val today = LocalDate.now()
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val endOfDay = date.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)
            
            coEvery { 
                taskDao.getCompletedTaskCountInDateRange(startOfDay, endOfDay) 
            } returns if (i < 3) 2 else 0
            coEvery { 
                taskDao.getTotalTaskCountInDateRange(startOfDay, endOfDay) 
            } returns if (i < 3) 3 else 0
        }
        
        // When
        val statistics = repository.getTaskStatistics()
        
        // Then
        assertEquals(15, statistics.totalTasks)
        assertEquals(10, statistics.completedTasks)
        assertEquals(66.67f, statistics.completionRate, 0.01f)
        assertEquals(3, statistics.currentStreak)
        assertEquals(3, statistics.categoryBreakdown.size)
        assertEquals(8, statistics.categoryBreakdown[Category.WORK])
        assertEquals(5, statistics.categoryBreakdown[Category.PERSONAL])
        assertEquals(2, statistics.categoryBreakdown[Category.SHOPPING])
        assertEquals(7, statistics.weeklyProgress.size)
    }
}