package com.strategy.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.strategy.note.data.NoteDatabase
import com.strategy.note.repository.NoteRepository
import com.strategy.note.ui.screen.HomeScreen
import com.strategy.note.ui.screen.TextEditorScreen
import com.strategy.note.ui.screen.ChecklistEditorScreen
import com.strategy.note.ui.theme.StrategyNoteTheme
import com.strategy.note.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { NoteDatabase.getDatabase(this) }
    private val repository by lazy { NoteRepository(database.noteDao()) }
    private val viewModel: NoteViewModel by viewModels {
        NoteViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StrategyNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StrategyNoteApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StrategyNoteApp(viewModel: NoteViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToTextEditor = { noteId ->
                    navController.navigate("text_editor/$noteId")
                },
                onNavigateToChecklistEditor = { noteId ->
                    navController.navigate("checklist_editor/$noteId")
                }
            )
        }
        composable(
            route = "text_editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            TextEditorScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTextEditor = { id -> navController.navigate("text_editor/$id") },
                onNavigateToChecklistEditor = { id -> navController.navigate("checklist_editor/$id") }
            )
        }
        composable(
            route = "checklist_editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            ChecklistEditorScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTextEditor = { id -> navController.navigate("text_editor/$id") },
                onNavigateToChecklistEditor = { id -> navController.navigate("checklist_editor/$id") }
            )
        }
    }
}
