package com.example.todoapp.domain.model

import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

class PriorityTest {

    @Test
    fun `Priority enum has correct display names`() {
        assertEquals("High", Priority.HIGH.displayName)
        assertEquals("Medium", Priority.MEDIUM.displayName)
        assertEquals("Low", Priority.LOW.displayName)
    }

    @Test
    fun `Priority enum has correct colors`() {
        assertEquals(Color(0xFFE53E3E), Priority.HIGH.color)
        assertEquals(Color(0xFFFF8C00), Priority.MEDIUM.color)
        assertEquals(Color(0xFF38A169), Priority.LOW.color)
    }

    @Test
    fun `Priority enum values are correct`() {
        val priorities = Priority.values()
        assertEquals(3, priorities.size)
        assertEquals(Priority.HIGH, priorities[0])
        assertEquals(Priority.MEDIUM, priorities[1])
        assertEquals(Priority.LOW, priorities[2])
    }
}