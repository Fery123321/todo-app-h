package com.example.todoapp.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utility class for date formatting and date-related operations
 */
object DateUtils {
    
    // Date formatters
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    private val longDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    
    /**
     * Formats a date for display in task items (e.g., "Jan 15")
     */
    fun formatShortDate(dateTime: LocalDateTime): String {
        return dateTime.format(shortDateFormatter)
    }
    
    /**
     * Formats a date for display in detailed views (e.g., "Jan 15, 2024")
     */
    fun formatLongDate(dateTime: LocalDateTime): String {
        return dateTime.format(longDateFormatter)
    }
    
    /**
     * Formats a date and time for display in forms (e.g., "Jan 15, 2024 at 3:30 PM")
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    /**
     * Formats time only (e.g., "3:30 PM")
     */
    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(timeFormatter)
    }
    
    /**
     * Checks if a date is overdue (before current date/time)
     */
    fun isOverdue(dueDate: LocalDateTime, isCompleted: Boolean = false): Boolean {
        return dueDate.isBefore(LocalDateTime.now()) && !isCompleted
    }
    
    /**
     * Checks if a date is due today
     */
    fun isDueToday(dueDate: LocalDateTime, isCompleted: Boolean = false): Boolean {
        return dueDate.toLocalDate() == LocalDate.now() && !isCompleted
    }
    
    /**
     * Checks if a date is due within the next 24 hours
     */
    fun isDueSoon(dueDate: LocalDateTime, isCompleted: Boolean = false): Boolean {
        if (isCompleted) return false
        val now = LocalDateTime.now()
        val hoursUntilDue = ChronoUnit.HOURS.between(now, dueDate)
        return hoursUntilDue in 0..24
    }
    
    /**
     * Gets a relative description of when a task is due
     */
    fun getRelativeDueDateDescription(dueDate: LocalDateTime, isCompleted: Boolean = false): String {
        if (isCompleted) return "Completed"
        
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val dueLocalDate = dueDate.toLocalDate()
        
        return when {
            isOverdue(dueDate, isCompleted) -> {
                val daysOverdue = ChronoUnit.DAYS.between(dueLocalDate, today)
                when (daysOverdue) {
                    0L -> "Overdue today"
                    1L -> "1 day overdue"
                    else -> "$daysOverdue days overdue"
                }
            }
            isDueToday(dueDate, isCompleted) -> {
                val hoursUntilDue = ChronoUnit.HOURS.between(now, dueDate)
                when {
                    hoursUntilDue <= 1 -> "Due in ${ChronoUnit.MINUTES.between(now, dueDate)} minutes"
                    hoursUntilDue < 24 -> "Due in $hoursUntilDue hours"
                    else -> "Due today"
                }
            }
            dueLocalDate == today.plusDays(1) -> "Due tomorrow"
            else -> {
                val daysUntilDue = ChronoUnit.DAYS.between(today, dueLocalDate)
                "Due in $daysUntilDue days"
            }
        }
    }
    
    /**
     * Gets the appropriate color indicator for a due date
     */
    enum class DueDateStatus {
        OVERDUE,    // Red indicator
        DUE_TODAY,  // Orange indicator  
        DUE_SOON,   // Yellow indicator
        NORMAL      // Default color
    }
    
    /**
     * Determines the status of a due date for color coding
     */
    fun getDueDateStatus(dueDate: LocalDateTime, isCompleted: Boolean = false): DueDateStatus {
        if (isCompleted) return DueDateStatus.NORMAL
        
        return when {
            isOverdue(dueDate, isCompleted) -> DueDateStatus.OVERDUE
            isDueToday(dueDate, isCompleted) -> DueDateStatus.DUE_TODAY
            isDueSoon(dueDate, isCompleted) -> DueDateStatus.DUE_SOON
            else -> DueDateStatus.NORMAL
        }
    }
}