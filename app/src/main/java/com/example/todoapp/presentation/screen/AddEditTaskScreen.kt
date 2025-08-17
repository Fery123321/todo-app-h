package com.example.todoapp.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todoapp.domain.model.Category
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.TodoTask
import com.example.todoapp.presentation.components.PriorityIndicator
import com.example.todoapp.presentation.components.TimePickerDialog
import com.example.todoapp.ui.theme.TodoAppTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    task: TodoTask? = null, // null for add, non-null for edit
    onNavigateBack: () -> Unit,
    onSaveTask: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = task != null
    
    // Form state
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(task?.category ?: Category.PERSONAL) }
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    
    // UI state
    var isPriorityDropdownExpanded by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Date and time picker states
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = dueDate?.hour ?: 12,
        initialMinute = dueDate?.minute ?: 0
    )
    
    // Validation
    val isTitleValid = title.trim().isNotBlank()
    val canSave = isTitleValid
    
    // Focus on title when screen opens for new tasks
    LaunchedEffect(Unit) {
        if (!isEditing) {
            focusRequester.requestFocus()
        }
    }
    
    fun saveTask() {
        if (canSave) {
            val taskToSave = if (isEditing) {
                task!!.copy(
                    title = title.trim(),
                    description = description.trim(),
                    priority = selectedPriority,
                    category = selectedCategory,
                    dueDate = dueDate
                )
            } else {
                TodoTask(
                    title = title.trim(),
                    description = description.trim(),
                    priority = selectedPriority,
                    category = selectedCategory,
                    dueDate = dueDate
                )
            }
            onSaveTask(taskToSave)
            onNavigateBack()
        }
    }
    
    fun formatDueDate(dateTime: LocalDateTime?): String {
        return dateTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")) ?: "No due date"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isEditing) "Edit Task" else "Add New Task") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { saveTask() },
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Save",
                    color = if (canSave) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("Enter task title...") },
                isError = title.isNotBlank() && !isTitleValid,
                supportingText = if (title.isNotBlank() && !isTitleValid) {
                    { Text("Title cannot be empty") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true
            )
            
            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter task description...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Priority dropdown
            ExposedDropdownMenuBox(
                expanded = isPriorityDropdownExpanded,
                onExpandedChange = { isPriorityDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPriority.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPriorityDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = isPriorityDropdownExpanded,
                    onDismissRequest = { isPriorityDropdownExpanded = false }
                ) {
                    Priority.entries.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.displayName) },
                            onClick = {
                                selectedPriority = priority
                                isPriorityDropdownExpanded = false
                            },
                            leadingIcon = {
                                PriorityIndicator(
                                    priority = priority,
                                    alpha = 1f
                                )
                            }
                        )
                    }
                }
            }
            
            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = isCategoryDropdownExpanded,
                onExpandedChange = { isCategoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false }
                ) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                isCategoryDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.displayName
                                )
                            }
                        )
                    }
                }
            }
            
            // Due date section
            OutlinedTextField(
                value = formatDueDate(dueDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date") },
                placeholder = { Text("Set due date...") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Clear due date button
            if (dueDate != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { dueDate = null }
                    ) {
                        Text("Clear Due Date")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            
                            dueDate = selectedDate.atTime(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun AddEditTaskScreenAddPreview() {
    TodoAppTheme {
        AddEditTaskScreen(
            task = null,
            onNavigateBack = {},
            onSaveTask = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditTaskScreenEditPreview() {
    TodoAppTheme {
        AddEditTaskScreen(
            task = TodoTask(
                title = "Existing Task",
                description = "This is an existing task to edit",
                priority = Priority.HIGH,
                category = Category.WORK,
                dueDate = LocalDateTime.now().plusDays(2)
            ),
            onNavigateBack = {},
            onSaveTask = {}
        )
    }
}