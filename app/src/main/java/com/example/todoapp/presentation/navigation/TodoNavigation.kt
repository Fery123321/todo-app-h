package com.example.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.presentation.screen.TaskListScreen

@Composable
fun TodoNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen()
        }
        // Additional routes will be added in later tasks
    }
}