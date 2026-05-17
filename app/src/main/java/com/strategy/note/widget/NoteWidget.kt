package com.strategy.note.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.strategy.note.data.NoteDatabase

class NoteWidget : GlanceAppWidget() {
    override suspend fun provideContent(context: Context, glanceId: GlanceId) {
        val database = NoteDatabase.getDatabase(context)
        val noteDao = database.noteDao()
        val latestNote = noteDao.getLatestNote()

        provideContent {
            WidgetContent(latestNote?.title ?: "No Note Available", latestNote?.content ?: "Add a new note inside the app!")
        }
    }

    @Composable
    private fun WidgetContent(title: String, content: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFFFF0B3)))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.Black)
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = content,
                style = TextStyle(
                    color = ColorProvider(Color.Black.copy(alpha = 0.8f))
                )
            )
        }
    }
}

class NoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NoteWidget()
}
