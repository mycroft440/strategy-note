package com.strategy.note.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.strategy.note.R
import com.strategy.note.data.Note
import com.strategy.note.data.ChecklistItem
import com.strategy.note.data.NoteType
import com.strategy.note.ui.theme.NoteColors
import com.strategy.note.ui.theme.getNoteColor
import com.strategy.note.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var colorCode by remember { mutableStateOf(NoteColors[4].toArgb().toLong()) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }
    var newItemText by remember { mutableStateOf("") }

    LaunchedEffect(noteId) {
        if (noteId > 0 && !isLoaded) {
            val note = viewModel.allNotes.value.find { it.id == noteId }
            if (note != null) {
                title = note.title
                colorCode = note.colorCode
                reminderTime = note.reminderTime
                viewModel.getChecklistItems(noteId).collect { items ->
                    checklistItems.clear()
                    checklistItems.addAll(items)
                }
            }
            isLoaded = true
        } else {
            isLoaded = true
        }
    }

    val saveCurrentChecklist = {
        if (title.isNotEmpty() || checklistItems.isNotEmpty()) {
            val note = Note(
                id = if (noteId > 0) noteId else 0,
                title = title,
                type = NoteType.CHECKLIST.value,
                colorCode = colorCode,
                reminderTime = reminderTime,
                modifiedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note, checklistItems.toList())
        }
    }

    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId > 0) "Edit Checklist" else "New Checklist",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        saveCurrentChecklist()
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showColorDialog = true }) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Change Color")
                    }
                    IconButton(onClick = {
                        showDateTimePicker(context) { selectedTime ->
                            reminderTime = selectedTime
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = "Set Reminder")
                    }
                    if (noteId > 0) {
                        IconButton(onClick = {
                            val note = viewModel.allNotes.value.find { it.id == noteId }
                            if (note != null) {
                                viewModel.deleteNote(context, note)
                                onNavigateBack()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getNoteColor(colorCode)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(getNoteColor(colorCode))
                .padding(16.dp)
        ) {
            if (reminderTime != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Reminder: " + SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(reminderTime!!)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(
                        onClick = { reminderTime = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Reminder",
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(stringResource(R.string.title_hint), color = Color.Black.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Add checklist item...", color = Color.Black.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.2f)
                    )
                )
                IconButton(
                    onClick = {
                        if (newItemText.isNotEmpty()) {
                            checklistItems.add(
                                ChecklistItem(
                                    noteId = noteId,
                                    text = newItemText,
                                    isChecked = false,
                                    position = checklistItems.size
                                )
                            )
                            newItemText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.1f))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(checklistItems) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { isChecked ->
                                checklistItems[index] = item.copy(isChecked = isChecked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Black.copy(alpha = 0.8f),
                                uncheckedColor = Color.Black.copy(alpha = 0.4f),
                                checkmarkColor = getNoteColor(colorCode)
                            )
                        )
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { checklistItems.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Item",
                                tint = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        if (showColorDialog) {
            AlertDialog(
                onDismissRequest = { showColorDialog = false },
                title = { Text("Choose Pastel Color") },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NoteColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        colorCode = color.toArgb().toLong()
                                        showColorDialog = false
                                    }
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}
