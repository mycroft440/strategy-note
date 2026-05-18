package com.strategy.note.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.strategy.note.ui.theme.DarkSurface
import com.strategy.note.viewmodel.NoteViewModel
import com.strategy.note.data.Notebook
import android.preference.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNavigateToTextEditor: (Int) -> Unit,
    onNavigateToChecklistEditor: (Int) -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isGridView by remember { mutableStateOf(true) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showNotebookDrawer by remember { mutableStateOf(false) }
    var showCreateNotebookDialog by remember { mutableStateOf(false) }
    var showRenameNotebookDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Notebook?>(null) }
    var newNotebookName by remember { mutableStateOf("") }
    var showAppSettingsDialog by remember { mutableStateOf(false) }

    val notebooks by viewModel.allNotebooks.collectAsState()
    val selectedNotebookId by viewModel.selectedNotebookId.collectAsState()
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }

    LaunchedEffect(Unit) {
        if (viewModel.isAutoOpenLastNotebook(prefs)) {
            val lastId = viewModel.getLastNotebookId(prefs)
            if (lastId > 0) {
                viewModel.selectNotebook(lastId)
            }
        }
    }

    LaunchedEffect(selectedNotebookId) {
        if (selectedNotebookId > 0) {
            viewModel.saveLastNotebookId(prefs, selectedNotebookId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val selectedNotebook = notebooks.find { it.id == selectedNotebookId }
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (selectedNotebook != null) {
                            Text(
                                text = selectedNotebook.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showNotebookDrawer = true }) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Cadernos")
                    }
                },
                actions = {
                    IconButton(onClick = { showAppSettingsDialog = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_notes)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )



            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            stringResource(R.string.no_results)
                        } else {
                            stringResource(R.string.empty_notes)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                AnimatedContent(
                    targetState = isGridView,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ViewToggle"
                ) { gridViewActive ->
                    if (gridViewActive) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        if (note.type == NoteType.CHECKLIST.value) {
                                            onNavigateToChecklistEditor(note.id)
                                        } else {
                                            onNavigateToTextEditor(note.id)
                                        }
                                    },
                                    onLongClick = { viewModel.deleteNote(context, note) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        if (note.type == NoteType.CHECKLIST.value) {
                                            onNavigateToChecklistEditor(note.id)
                                        } else {
                                            onNavigateToTextEditor(note.id)
                                        }
                                    },
                                    onLongClick = { viewModel.deleteNote(context, note) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddNoteDialog) {
            AlertDialog(
                onDismissRequest = { showAddNoteDialog = false },
                title = { Text("Choose Note Type") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                showAddNoteDialog = false
                                onNavigateToTextEditor(0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notes, contentDescription = null)
                                Text("Text Note")
                            }
                        }

                        Button(
                            onClick = {
                                showAddNoteDialog = false
                                onNavigateToChecklistEditor(0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = null)
                                Text("Checklist Note")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Notebook Drawer Dialog
        if (showNotebookDrawer) {
            AlertDialog(
                onDismissRequest = { showNotebookDrawer = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cadernos", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showCreateNotebookDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Caderno")
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // "All Notes" option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedNotebookId == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.selectNotebook(0)
                                    showNotebookDrawer = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Todas as Notas", fontWeight = if (selectedNotebookId == 0) FontWeight.Bold else FontWeight.Normal)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        notebooks.forEach { notebook ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedNotebookId == notebook.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        viewModel.selectNotebook(notebook.id)
                                        showNotebookDrawer = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(notebook.name, fontWeight = if (selectedNotebookId == notebook.id) FontWeight.Bold else FontWeight.Normal)
                                }
                                Row {
                                    IconButton(onClick = {
                                        renameTarget = notebook
                                        newNotebookName = notebook.name
                                        showRenameNotebookDialog = true
                                    }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Renomear", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = {
                                        viewModel.deleteNotebook(notebook)
                                        if (selectedNotebookId == notebook.id) {
                                            viewModel.selectNotebook(0)
                                        }
                                    }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotebookDrawer = false }) {
                        Text("Fechar")
                    }
                }
            )
        }

        // Create Notebook Dialog
        if (showCreateNotebookDialog) {
            AlertDialog(
                onDismissRequest = { showCreateNotebookDialog = false },
                title = { Text("Novo Caderno", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newNotebookName,
                        onValueChange = { newNotebookName = it },
                        placeholder = { Text("Nome do caderno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newNotebookName.isNotBlank()) {
                            viewModel.createNotebook(newNotebookName.trim()) { id ->
                                viewModel.selectNotebook(id)
                            }
                            newNotebookName = ""
                            showCreateNotebookDialog = false
                            showNotebookDrawer = false
                        }
                    }) {
                        Text("Criar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateNotebookDialog = false; newNotebookName = "" }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Rename Notebook Dialog
        if (showRenameNotebookDialog && renameTarget != null) {
            AlertDialog(
                onDismissRequest = { showRenameNotebookDialog = false },
                title = { Text("Renomear Caderno", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newNotebookName,
                        onValueChange = { newNotebookName = it },
                        placeholder = { Text("Novo nome") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newNotebookName.isNotBlank()) {
                            viewModel.renameNotebook(renameTarget!!, newNotebookName.trim())
                            newNotebookName = ""
                            showRenameNotebookDialog = false
                        }
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameNotebookDialog = false; newNotebookName = "" }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // App Settings Dialog
        if (showAppSettingsDialog) {
            var autoOpen by remember { mutableStateOf(viewModel.isAutoOpenLastNotebook(prefs)) }
            AlertDialog(
                onDismissRequest = { showAppSettingsDialog = false },
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sempre abrir no último caderno",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = autoOpen,
                                onCheckedChange = {
                                    autoOpen = it
                                    viewModel.setAutoOpenLastNotebook(prefs, it)
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAppSettingsDialog = false }) {
                        Text("Fechar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = getDarkNoteCardColor(note.colorCode)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getDarkNoteAccentColor(note.colorCode),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.reminderTime != null) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Reminder Set",
                        tint = getDarkNoteAccentColor(note.colorCode).copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (note.type == NoteType.CHECKLIST.value) {
                    "[Checklist Note]"
                } else {
                    note.content.ifEmpty { "Empty note" }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DarkOnSurface.copy(alpha = 0.8f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onLongClick()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
