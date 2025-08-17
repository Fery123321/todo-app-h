package com.example.todoapp.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import org.junit.Test
import org.junit.Assert.*

class CategoryTest {

    @Test
    fun `Category enum has correct display names`() {
        assertEquals("Work", Category.WORK.displayName)
        assertEquals("Personal", Category.PERSONAL.displayName)
        assertEquals("Shopping", Category.SHOPPING.displayName)
        assertEquals("Health", Category.HEALTH.displayName)
        assertEquals("Education", Category.EDUCATION.displayName)
    }

    @Test
    fun `Category enum has correct icons`() {
        assertEquals(Icons.Default.Home, Category.WORK.icon)
        assertEquals(Icons.Default.Person, Category.PERSONAL.icon)
        assertEquals(Icons.Default.ShoppingCart, Category.SHOPPING.icon)
        assertEquals(Icons.Default.Favorite, Category.HEALTH.icon)
        assertEquals(Icons.Default.Star, Category.EDUCATION.icon)
    }

    @Test
    fun `Category enum values are correct`() {
        val categories = Category.values()
        assertEquals(5, categories.size)
        assertEquals(Category.WORK, categories[0])
        assertEquals(Category.PERSONAL, categories[1])
        assertEquals(Category.SHOPPING, categories[2])
        assertEquals(Category.HEALTH, categories[3])
        assertEquals(Category.EDUCATION, categories[4])
    }
}