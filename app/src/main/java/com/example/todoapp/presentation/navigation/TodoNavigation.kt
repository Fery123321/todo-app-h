package com.example.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.presentation.TodoViewModel
import com.example.todoapp.presentation.screen.StatisticsScreen
import com.example.todoapp.presentation.screen.TaskListScreen

@Composable
fun TodoNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                onNavigateToStatistics = {
                    navController.navigate("statistics")
                }
            )
        }
        
        composable("statistics") {
            val viewModel: TodoViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            StatisticsScreen(
                statistics = uiState.statistics,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}