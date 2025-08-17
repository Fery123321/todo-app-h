package com.example.todoapp.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class DateUtilsTest {

    @Test
    fun `formatShortDate returns correct format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val result = DateUtils.formatShortDate(dateTime)
        assertEquals("Jan 15", result)
    }

    @Test
    fun `formatLongDate returns correct format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val result = DateUtils.formatLongDate(dateTime)
        assertEquals("Jan 15, 2024", result)
    }

    @Test
    fun `formatDateTime returns correct format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val result = DateUtils.formatDateTime(dateTime)
        assertEquals("Jan 15, 2024 at 2:30 PM", result)
    }

    @Test
    fun `formatTime returns correct format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val result = DateUtils.formatTime(dateTime)
        assertEquals("2:30 PM", result)
    }

    @Test
    fun `isOverdue returns true for past date when not completed`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertTrue(DateUtils.isOverdue(pastDate, false))
    }

    @Test
    fun `isOverdue returns false for past date when completed`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertFalse(DateUtils.isOverdue(pastDate, true))
    }

    @Test
    fun `isOverdue returns false for future date`() {
        val futureDate = LocalDateTime.now().plusDays(1)
        assertFalse(DateUtils.isOverdue(futureDate, false))
    }

    @Test
    fun `isDueToday returns true for today's date when not completed`() {
        val todayDate = LocalDateTime.now().withHour(23).withMinute(59)
        assertTrue(DateUtils.isDueToday(todayDate, false))
    }

    @Test
    fun `isDueToday returns false for today's date when completed`() {
        val todayDate = LocalDateTime.now().withHour(23).withMinute(59)
        assertFalse(DateUtils.isDueToday(todayDate, true))
    }

    @Test
    fun `isDueToday returns false for tomorrow's date`() {
        val tomorrowDate = LocalDateTime.now().plusDays(1)
        assertFalse(DateUtils.isDueToday(tomorrowDate, false))
    }

    @Test
    fun `isDueSoon returns true for date within 24 hours when not completed`() {
        val soonDate = LocalDateTime.now().plusHours(12)
        assertTrue(DateUtils.isDueSoon(soonDate, false))
    }

    @Test
    fun `isDueSoon returns false for date within 24 hours when completed`() {
        val soonDate = LocalDateTime.now().plusHours(12)
        assertFalse(DateUtils.isDueSoon(soonDate, true))
    }

    @Test
    fun `isDueSoon returns false for date beyond 24 hours`() {
        val farDate = LocalDateTime.now().plusHours(30)
        assertFalse(DateUtils.isDueSoon(farDate, false))
    }

    @Test
    fun `getDueDateStatus returns OVERDUE for past date when not completed`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertEquals(DateUtils.DueDateStatus.OVERDUE, DateUtils.getDueDateStatus(pastDate, false))
    }

    @Test
    fun `getDueDateStatus returns DUE_TODAY for today's date when not completed`() {
        val todayDate = LocalDateTime.now().withHour(23).withMinute(59)
        assertEquals(DateUtils.DueDateStatus.DUE_TODAY, DateUtils.getDueDateStatus(todayDate, false))
    }

    @Test
    fun `getDueDateStatus returns DUE_SOON for date within 24 hours when not completed`() {
        val soonDate = LocalDateTime.now().plusHours(12)
        assertEquals(DateUtils.DueDateStatus.DUE_SOON, DateUtils.getDueDateStatus(soonDate, false))
    }

    @Test
    fun `getDueDateStatus returns NORMAL for future date beyond 24 hours when not completed`() {
        val futureDate = LocalDateTime.now().plusDays(2)
        assertEquals(DateUtils.DueDateStatus.NORMAL, DateUtils.getDueDateStatus(futureDate, false))
    }

    @Test
    fun `getDueDateStatus returns NORMAL for any date when completed`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertEquals(DateUtils.DueDateStatus.NORMAL, DateUtils.getDueDateStatus(pastDate, true))
    }

    @Test
    fun `getRelativeDueDateDescription returns correct description for overdue task`() {
        val pastDate = LocalDateTime.now().minusDays(2)
        val result = DateUtils.getRelativeDueDateDescription(pastDate, false)
        assertEquals("2 days overdue", result)
    }

    @Test
    fun `getRelativeDueDateDescription returns correct description for today overdue task`() {
        val todayPastDate = LocalDateTime.now().minusHours(2)
        val result = DateUtils.getRelativeDueDateDescription(todayPastDate, false)
        assertEquals("Overdue today", result)
    }

    @Test
    fun `getRelativeDueDateDescription returns correct description for due today task`() {
        val todayFutureDate = LocalDateTime.now().plusHours(3)
        val result = DateUtils.getRelativeDueDateDescription(todayFutureDate, false)
        assertTrue("Expected result to contain 'Due in' and 'hours', but got: $result", 
            result.contains("Due in") && result.contains("hours"))
    }

    @Test
    fun `getRelativeDueDateDescription returns correct description for due tomorrow task`() {
        val tomorrowDate = LocalDateTime.now().plusDays(1)
        val result = DateUtils.getRelativeDueDateDescription(tomorrowDate, false)
        assertEquals("Due tomorrow", result)
    }

    @Test
    fun `getRelativeDueDateDescription returns correct description for future task`() {
        val futureDate = LocalDateTime.now().plusDays(5)
        val result = DateUtils.getRelativeDueDateDescription(futureDate, false)
        assertEquals("Due in 5 days", result)
    }

    @Test
    fun `getRelativeDueDateDescription returns Completed for completed task`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        val result = DateUtils.getRelativeDueDateDescription(pastDate, true)
        assertEquals("Completed", result)
    }
}