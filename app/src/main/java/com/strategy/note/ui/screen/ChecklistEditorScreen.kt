package com.strategy.note.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import com.strategy.note.data.NoteType
import com.strategy.note.data.ChecklistItem
import com.strategy.note.ui.theme.NoteColors
import com.strategy.note.ui.theme.getNoteColor
import com.strategy.note.ui.theme.getDarkNoteCardColor
import com.strategy.note.ui.theme.getDarkNoteAccentColor
import com.strategy.note.ui.theme.DarkOnSurface
import com.strategy.note.ui.theme.DarkOnSurfaceVariant
import com.strategy.note.ui.theme.DarkSurface
import com.strategy.note.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTextEditor: (Int) -> Unit = {},
    onNavigateToChecklistEditor: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var colorCode by remember { mutableStateOf(NoteColors[4].toArgb().toLong()) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isLoaded by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf(noteId) }
    val focusRequester = remember { FocusRequester() }

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
            currentNoteId = noteId
            isLoaded = true
        } else {
            isLoaded = true
        }
    }

    LaunchedEffect(isLoaded) {
        if (isLoaded && noteId <= 0) {
            focusRequester.requestFocus()
        }
    }

    val saveCurrentChecklist = {
        val finalTitle = if (title.trim().isEmpty() && checklistItems.isNotEmpty()) {
            viewModel.generateDefaultTitle()
        } else {
            title.trim()
        }
        
        if (finalTitle.isNotEmpty() || checklistItems.isNotEmpty()) {
            val note = Note(
                id = if (currentNoteId > 0) currentNoteId else 0,
                title = finalTitle,
                type = NoteType.CHECKLIST.value,
                colorCode = colorCode,
                reminderTime = reminderTime,
                modifiedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note, checklistItems.toList()) { insertedId ->
                currentNoteId = insertedId
            }
        }
    }

    BackHandler {
        saveCurrentChecklist()
        onNavigateBack()
    }

    LaunchedEffect(title, checklistItems.toList()) {
        if (!isLoaded) return@LaunchedEffect
        if (title.isNotEmpty() || checklistItems.isNotEmpty()) {
            delay(1000)
            val finalTitle = if (title.trim().isEmpty() && checklistItems.isNotEmpty()) {
                viewModel.generateDefaultTitle()
            } else {
                title.trim()
            }
            val note = Note(
                id = if (currentNoteId > 0) currentNoteId else 0,
                title = finalTitle,
                type = NoteType.CHECKLIST.value,
                colorCode = colorCode,
                reminderTime = reminderTime,
                modifiedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note, checklistItems.toList()) { insertedId ->
                currentNoteId = insertedId
            }
        }
    }

    var showColorDialog by remember { mutableStateOf(false) }
    var showLinkSubnoteDialog by remember { mutableStateOf(false) }
    val subnotes by viewModel.getSubnotes(currentNoteId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId > 0) "Edit Checklist" else "Digite o Título",
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Add checklist item...", color = DarkOnSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkOnSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = getDarkNoteAccentColor(colorCode).copy(alpha = 0.8f),
                        unfocusedBorderColor = DarkOnSurfaceVariant.copy(alpha = 0.3f)
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
                        .background(getDarkNoteAccentColor(colorCode).copy(alpha = 0.2f))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = getDarkNoteAccentColor(colorCode))
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
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { isChecked ->
                                checklistItems[index] = item.copy(isChecked = isChecked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = getDarkNoteAccentColor(colorCode),
                                uncheckedColor = DarkOnSurfaceVariant.copy(alpha = 0.6f),
                                checkmarkColor = DarkSurface
                            )
                        )
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkOnSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { checklistItems.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Item",
                                tint = DarkOnSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                if (currentNoteId > 0) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = DarkOnSurfaceVariant.copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subnotas Relacionadas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = getDarkNoteAccentColor(colorCode)
                            )
                            IconButton(onClick = { showLinkSubnoteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Adicionar Subnota",
                                    tint = getDarkNoteAccentColor(colorCode)
                                )
                            }
                        }
                    }
                    
                    if (subnotes.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma subnota vinculada. Toque no + para criar ou vincular uma!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkOnSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(subnotes.size) { index ->
                            val subnote = subnotes[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (subnote.type == NoteType.TEXT.value) {
                                            onNavigateToTextEditor(subnote.id)
                                        } else {
                                            onNavigateToChecklistEditor(subnote.id)
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = getDarkNoteCardColor(subnote.colorCode)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subnote.title.ifEmpty { "Sem Título" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = getDarkNoteAccentColor(subnote.colorCode)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (subnote.type == NoteType.TEXT.value) "Nota de Texto" else "Checklist",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkOnSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.unlinkSubnote(currentNoteId, subnote.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LinkOff,
                                            contentDescription = "Desvincular",
                                            tint = DarkOnSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLinkSubnoteDialog) {
            val allNotesList by viewModel.allNotes.collectAsState()
            val subnotesList by viewModel.getSubnotes(currentNoteId).collectAsState(initial = emptyList())
            val childIds = subnotesList.map { it.id }.toSet()
            val linkableNotes = allNotesList.filter { it.id != currentNoteId && !childIds.contains(it.id) }
            
            AlertDialog(
                onDismissRequest = { showLinkSubnoteDialog = false },
                title = {
                    Text(
                        text = "Adicionar Subnota",
                        fontWeight = FontWeight.Bold,
                        color = getDarkNoteAccentColor(colorCode)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Criar nova subnota vinculada:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkOnSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val newNote = Note(
                                        title = "",
                                        type = NoteType.TEXT.value,
                                        colorCode = colorCode,
                                        modifiedAt = System.currentTimeMillis()
                                    )
                                    viewModel.saveNote(context, newNote) { insertedId ->
                                        viewModel.linkSubnote(currentNoteId, insertedId)
                                        showLinkSubnoteDialog = false
                                        onNavigateToTextEditor(insertedId)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = getDarkNoteAccentColor(colorCode)
                                )
                            ) {
                                Text("Nova Nota", color = Color.Black)
                            }
                            Button(
                                onClick = {
                                    val newNote = Note(
                                        title = "",
                                        type = NoteType.CHECKLIST.value,
                                        colorCode = colorCode,
                                        modifiedAt = System.currentTimeMillis()
                                    )
                                    viewModel.saveNote(context, newNote, emptyList()) { insertedId ->
                                        viewModel.linkSubnote(currentNoteId, insertedId)
                                        showLinkSubnoteDialog = false
                                        onNavigateToChecklistEditor(insertedId)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = getDarkNoteAccentColor(colorCode)
                                )
                            ) {
                                Text("Novo Checklist", color = Color.Black)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = DarkOnSurfaceVariant.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Ou vincular nota existente:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkOnSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (linkableNotes.isEmpty()) {
                            Text(
                                text = "Nenhuma outra nota disponível para vincular.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkOnSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(linkableNotes.size) { index ->
                                    val linkable = linkableNotes[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(getDarkNoteCardColor(linkable.colorCode))
                                            .clickable {
                                                viewModel.linkSubnote(currentNoteId, linkable.id)
                                                showLinkSubnoteDialog = false
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = linkable.title.ifEmpty { "Sem Título" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = getDarkNoteAccentColor(linkable.colorCode)
                                        )
                                        Text(
                                            text = if (linkable.type == NoteType.TEXT.value) "Texto" else "Checklist",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkOnSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLinkSubnoteDialog = false }) {
                        Text("Cancelar", color = getDarkNoteAccentColor(colorCode))
                    }
                }
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
