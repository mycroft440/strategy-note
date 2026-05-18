package com.strategy.note.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.strategy.note.data.NoteType
import com.strategy.note.ui.theme.NoteColors
import com.strategy.note.ui.theme.getNoteColor
import com.strategy.note.ui.theme.getDarkNoteCardColor
import com.strategy.note.ui.theme.getDarkNoteAccentColor
import com.strategy.note.ui.theme.DarkOnSurface
import com.strategy.note.ui.theme.DarkOnSurfaceVariant
import com.strategy.note.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorCode by remember { mutableStateOf(NoteColors[2].toArgb().toLong()) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isLoaded by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf(noteId) }
    val focusRequester = remember { FocusRequester() }

    // Undo / Redo History State Management
    val undoStack = remember { mutableStateListOf<Pair<String, String>>() }
    val redoStack = remember { mutableStateListOf<Pair<String, String>>() }
    var isUndoRedoAction by remember { mutableStateOf(false) }
    var lastPushedState by remember { mutableStateOf(Pair("", "")) }

    LaunchedEffect(noteId) {
        if (noteId > 0 && !isLoaded) {
            val note = viewModel.allNotes.value.find { it.id == noteId }
            if (note != null) {
                title = note.title
                content = note.content
                colorCode = note.colorCode
                reminderTime = note.reminderTime
                lastPushedState = Pair(note.title, note.content)
            }
            currentNoteId = noteId
            isLoaded = true
        } else {
            isLoaded = true
        }
    }

    LaunchedEffect(title, content) {
        if (!isLoaded) return@LaunchedEffect
        
        if (isUndoRedoAction) {
            isUndoRedoAction = false
            return@LaunchedEffect
        }

        val currentState = Pair(title, content)
        if (currentState != lastPushedState) {
            val contentDiffersSpace = content.endsWith(" ") || content.endsWith("\n") || title.endsWith(" ")
            
            if (contentDiffersSpace) {
                if (undoStack.size >= 100) undoStack.removeAt(0)
                undoStack.add(lastPushedState)
                redoStack.clear()
                lastPushedState = currentState
            } else {
                delay(500)
                if (Pair(title, content) == currentState) {
                    if (undoStack.size >= 100) undoStack.removeAt(0)
                    undoStack.add(lastPushedState)
                    redoStack.clear()
                    lastPushedState = currentState
                }
            }
        }
    }

    val performUndo = {
        if (undoStack.isNotEmpty()) {
            val previousState = undoStack.removeLast()
            redoStack.add(Pair(title, content))
            
            isUndoRedoAction = true
            title = previousState.first
            content = previousState.second
            lastPushedState = previousState
        }
    }

    val performRedo = {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeLast()
            undoStack.add(Pair(title, content))
            
            isUndoRedoAction = true
            title = nextState.first
            content = nextState.second
            lastPushedState = nextState
        }
    }

    LaunchedEffect(isLoaded) {
        if (isLoaded && noteId <= 0) {
            focusRequester.requestFocus()
        }
    }

    val saveCurrentNote = {
        val finalTitle = if (title.trim().isEmpty() && content.trim().isNotEmpty()) {
            viewModel.generateDefaultTitle()
        } else {
            title.trim()
        }
        
        if (finalTitle.isNotEmpty() || content.trim().isNotEmpty()) {
            val note = Note(
                id = if (currentNoteId > 0) currentNoteId else 0,
                title = finalTitle,
                content = content,
                type = NoteType.TEXT.value,
                colorCode = colorCode,
                reminderTime = reminderTime,
                modifiedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note) { insertedId ->
                currentNoteId = insertedId
            }
        }
    }

    BackHandler {
        saveCurrentNote()
        onNavigateBack()
    }

    LaunchedEffect(title, content) {
        if (!isLoaded) return@LaunchedEffect
        if (title.isNotEmpty() || content.isNotEmpty()) {
            delay(1000)
            val finalTitle = if (title.trim().isEmpty() && content.trim().isNotEmpty()) {
                viewModel.generateDefaultTitle()
            } else {
                title.trim()
            }
            val note = Note(
                id = if (currentNoteId > 0) currentNoteId else 0,
                title = finalTitle,
                content = content,
                type = NoteType.TEXT.value,
                colorCode = colorCode,
                reminderTime = reminderTime,
                modifiedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note) { insertedId ->
                currentNoteId = insertedId
            }
        }
    }

    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId > 0) "Edit Note" else "Digite o Título",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        saveCurrentNote()
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = performUndo,
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(
                        onClick = performRedo,
                        enabled = redoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                        )
                    }
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
                    containerColor = getDarkNoteCardColor(colorCode)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(getDarkNoteCardColor(colorCode))
                .padding(16.dp)
        ) {
            if (reminderTime != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
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
                            tint = getDarkNoteAccentColor(colorCode).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Reminder: " + SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(reminderTime!!)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkOnSurface.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { reminderTime = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Reminder",
                            tint = getDarkNoteAccentColor(colorCode).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(stringResource(R.string.title_hint), color = DarkOnSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = getDarkNoteAccentColor(colorCode)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(stringResource(R.string.content_hint), color = DarkOnSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkOnSurface),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
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

fun showDateTimePicker(context: Context, onDateTimeSelected: (Long) -> Unit) {
    val currentCalendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(Calendar.YEAR, year)
            selectedCalendar.set(Calendar.MONTH, month)
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    selectedCalendar.set(Calendar.SECOND, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)

                    onDateTimeSelected(selectedCalendar.timeInMillis)
                },
                currentCalendar.get(Calendar.HOUR_OF_DAY),
                currentCalendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        currentCalendar.get(Calendar.YEAR),
        currentCalendar.get(Calendar.MONTH),
        currentCalendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
