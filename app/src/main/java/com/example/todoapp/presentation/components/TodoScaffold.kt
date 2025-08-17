package com.example.todoapp.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.todoapp.presentation.navigation.NavigationState

/**
 * Main scaffold for the Todo app with integrated top app bar and error handling
 */
@Composable
fun TodoScaffold(
    navigationState: NavigationState,
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    isSearchActive: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TodoTopAppBar(
                    title = navigationState.title,
                    canNavigateBack = navigationState.canNavigateBack,
                    showSearchAction = navigationState.currentRoute == "task_list",
                    showStatisticsAction = navigationState.currentRoute == "task_list",
                    isSearchActive = isSearchActive,
                    onNavigateBack = onNavigateBack,
                    onSearchClick = onSearchClick,
                    onStatisticsClick = onStatisticsClick
                )
            },
            snackbarHost = {
                TodoSnackbarHost(hostState = snackbarHostState)
            },
            containerColor = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}