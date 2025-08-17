package com.example.todoapp.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class TaskStatisticsTest {

    @Test
    fun `TaskStatistics creation with default values`() {
        // When
        val statistics = TaskStatistics()

        // Then
        assertEquals(0, statistics.totalTasks)
        assertEquals(0, statistics.completedTasks)
        assertEquals(0f, statistics.completionRate)
        assertEquals(0, statistics.currentStreak)
        assertTrue(statistics.categoryBreakdown.isEmpty())
        assertTrue(statistics.weeklyProgress.isEmpty())
    }

    @Test
    fun `TaskStatistics creation with custom values`() {
        // Given
        val totalTasks = 10
        val completedTasks = 7
        val completionRate = 0.7f
        val currentStreak = 5
        val categoryBreakdown = mapOf(
            Category.WORK to 4,
            Category.PERSONAL to 3,
            Category.SHOPPING to 3
        )
        val weeklyProgress = listOf(
            DailyProgress(LocalDate.now().minusDays(6), 2, 3),
            DailyProgress(LocalDate.now().minusDays(5), 1, 2),
            DailyProgress(LocalDate.now().minusDays(4), 3, 3)
        )

        // When
        val statistics = TaskStatistics(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionRate = completionRate,
            currentStreak = currentStreak,
            categoryBreakdown = categoryBreakdown,
            weeklyProgress = weeklyProgress
        )

        // Then
        assertEquals(totalTasks, statistics.totalTasks)
        assertEquals(completedTasks, statistics.completedTasks)
        assertEquals(completionRate, statistics.completionRate)
        assertEquals(currentStreak, statistics.currentStreak)
        assertEquals(categoryBreakdown, statistics.categoryBreakdown)
        assertEquals(weeklyProgress, statistics.weeklyProgress)
    }

    @Test
    fun `TaskStatistics copy with modifications`() {
        // Given
        val originalStats = TaskStatistics(
            totalTasks = 5,
            completedTasks = 3,
            completionRate = 0.6f
        )

        // When
        val modifiedStats = originalStats.copy(
            completedTasks = 4,
            completionRate = 0.8f,
            currentStreak = 2
        )

        // Then
        assertEquals(5, modifiedStats.totalTasks)
        assertEquals(4, modifiedStats.completedTasks)
        assertEquals(0.8f, modifiedStats.completionRate)
        assertEquals(2, modifiedStats.currentStreak)
    }
}