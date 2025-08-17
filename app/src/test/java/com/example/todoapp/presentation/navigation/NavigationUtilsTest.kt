package com.example.todoapp.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for navigation utilities
 */
class NavigationUtilsTest {

    @Test
    fun todoRouteBuilders_editTask_buildsCorrectRoute() {
        val taskId = "test-task-id"
        val expectedRoute = "edit_task/test-task-id"
        
        val actualRoute = TodoRouteBuilders.editTask(taskId)
        
        assertEquals(expectedRoute, actualRoute)
    }

    @Test
    fun deepLinkUtils_createTaskDeepLink_createsCorrectLink() {
        val taskId = "test-task-id"
        val expectedLink = "todoapp://task/edit_task/test-task-id"
        
        val actualLink = DeepLinkUtils.createTaskDeepLink(taskId)
        
        assertEquals(expectedLink, actualLink)
    }

    @Test
    fun deepLinkUtils_parseTaskIdFromDeepLink_parsesCorrectId() {
        val deepLink = "todoapp://task/edit_task/test-task-id"
        val expectedTaskId = "test-task-id"
        
        val actualTaskId = DeepLinkUtils.parseTaskIdFromDeepLink(deepLink)
        
        assertEquals(expectedTaskId, actualTaskId)
    }

    @Test
    fun deepLinkUtils_parseTaskIdFromDeepLink_returnsNullForInvalidLink() {
        val invalidDeepLink = "invalid://link"
        
        val taskId = DeepLinkUtils.parseTaskIdFromDeepLink(invalidDeepLink)
        
        assertNull(taskId)
    }

    @Test
    fun navigationState_defaultValues_areCorrect() {
        val navigationState = NavigationState()
        
        assertEquals(TodoDestinations.TASK_LIST, navigationState.currentRoute)
        assertFalse(navigationState.canNavigateBack)
        assertEquals(TodoScreenTitles.TASK_LIST, navigationState.title)
    }

    @Test
    fun navigationState_customValues_areSetCorrectly() {
        val navigationState = NavigationState(
            currentRoute = TodoDestinations.ADD_TASK,
            canNavigateBack = true,
            title = TodoScreenTitles.ADD_TASK
        )
        
        assertEquals(TodoDestinations.ADD_TASK, navigationState.currentRoute)
        assertTrue(navigationState.canNavigateBack)
        assertEquals(TodoScreenTitles.ADD_TASK, navigationState.title)
    }

    @Test
    fun todoScreenTitles_haveCorrectValues() {
        assertEquals("Tasks", TodoScreenTitles.TASK_LIST)
        assertEquals("Add Task", TodoScreenTitles.ADD_TASK)
        assertEquals("Edit Task", TodoScreenTitles.EDIT_TASK)
        assertEquals("Statistics", TodoScreenTitles.STATISTICS)
    }

    @Test
    fun todoDestinations_haveCorrectValues() {
        assertEquals("task_list", TodoDestinations.TASK_LIST)
        assertEquals("add_task", TodoDestinations.ADD_TASK)
        assertEquals("edit_task", TodoDestinations.EDIT_TASK)
        assertEquals("statistics", TodoDestinations.STATISTICS)
    }

    @Test
    fun todoRoutes_haveCorrectValues() {
        assertEquals("task_list", TodoRoutes.TASK_LIST)
        assertEquals("add_task", TodoRoutes.ADD_TASK)
        assertEquals("edit_task/{taskId}", TodoRoutes.EDIT_TASK)
        assertEquals("statistics", TodoRoutes.STATISTICS)
    }

    @Test
    fun todoArgs_haveCorrectValues() {
        assertEquals("taskId", TodoArgs.TASK_ID)
    }
}