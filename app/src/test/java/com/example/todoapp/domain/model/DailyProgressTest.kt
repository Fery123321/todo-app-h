package com.example.todoapp.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class DailyProgressTest {

    @Test
    fun `DailyProgress creation`() {
        // Given
        val date = LocalDate.now()
        val completedTasks = 5
        val totalTasks = 8

        // When
        val dailyProgress = DailyProgress(
            date = date,
            completedTasks = completedTasks,
            totalTasks = totalTasks
        )

        // Then
        assertEquals(date, dailyProgress.date)
        assertEquals(completedTasks, dailyProgress.completedTasks)
        assertEquals(totalTasks, dailyProgress.totalTasks)
    }

    @Test
    fun `DailyProgress copy with modifications`() {
        // Given
        val originalProgress = DailyProgress(
            date = LocalDate.now(),
            completedTasks = 3,
            totalTasks = 5
        )

        // When
        val modifiedProgress = originalProgress.copy(
            completedTasks = 4,
            totalTasks = 6
        )

        // Then
        assertEquals(originalProgress.date, modifiedProgress.date)
        assertEquals(4, modifiedProgress.completedTasks)
        assertEquals(6, modifiedProgress.totalTasks)
    }

    @Test
    fun `DailyProgress with zero values`() {
        // Given
        val date = LocalDate.now()

        // When
        val dailyProgress = DailyProgress(
            date = date,
            completedTasks = 0,
            totalTasks = 0
        )

        // Then
        assertEquals(date, dailyProgress.date)
        assertEquals(0, dailyProgress.completedTasks)
        assertEquals(0, dailyProgress.totalTasks)
    }
}