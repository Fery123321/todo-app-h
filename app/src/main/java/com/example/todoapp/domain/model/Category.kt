package com.example.todoapp.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val icon: ImageVector) {
    WORK("Work", Icons.Default.Home),
    PERSONAL("Personal", Icons.Default.Person),
    SHOPPING("Shopping", Icons.Default.ShoppingCart),
    HEALTH("Health", Icons.Default.Favorite),
    EDUCATION("Education", Icons.Default.Star)
}