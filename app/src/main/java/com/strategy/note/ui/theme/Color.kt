package com.strategy.note.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Dark Theme Palette
val DarkSurface = Color(0xFF121212)
val DarkBackground = Color(0xFF0D0D0D)
val DarkSurfaceVariant = Color(0xFF1E1E2E)
val DarkOnSurface = Color(0xFFE6E1E5)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
val DarkPrimary = Color(0xFFBB86FC)
val DarkOnPrimary = Color(0xFF1E1E2E)
val DarkSecondary = Color(0xFF03DAC6)
val DarkTertiary = Color(0xFFCF6679)
val DarkOutline = Color(0xFF49454F)

// Legacy compatibility
val Purple80 = DarkPrimary
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Pastel colors for notes (rich dark-mode adapted - more saturated, deeper tones)
val PastelRed = Color(0xFFE57373)
val PastelOrange = Color(0xFFFFB74D)
val PastelYellow = Color(0xFFFFD54F)
val PastelGreen = Color(0xFF81C784)
val PastelBlue = Color(0xFF64B5F6)
val PastelPurple = Color(0xFFBA68C8)
val PastelGrey = Color(0xFF90A4AE)

// Dark-mode note card backgrounds (deep rich tones)
val DarkNoteRed = Color(0xFF4A2020)
val DarkNoteOrange = Color(0xFF4A3320)
val DarkNoteYellow = Color(0xFF4A4520)
val DarkNoteGreen = Color(0xFF204A20)
val DarkNoteBlue = Color(0xFF20334A)
val DarkNotePurple = Color(0xFF3A204A)
val DarkNoteGrey = Color(0xFF2A2A2E)

val NoteColors = listOf(
    PastelRed, PastelOrange, PastelYellow, PastelGreen,
    PastelBlue, PastelPurple, PastelGrey
)

val DarkNoteCardColors = listOf(
    DarkNoteRed, DarkNoteOrange, DarkNoteYellow, DarkNoteGreen,
    DarkNoteBlue, DarkNotePurple, DarkNoteGrey
)

fun getDarkNoteCardColor(colorCode: Long): Color {
    val pastel = Color(colorCode)
    return when (pastel) {
        PastelRed -> DarkNoteRed
        PastelOrange -> DarkNoteOrange
        PastelYellow -> DarkNoteYellow
        PastelGreen -> DarkNoteGreen
        PastelBlue -> DarkNoteBlue
        PastelPurple -> DarkNotePurple
        PastelGrey -> DarkNoteGrey
        else -> DarkNoteGrey
    }
}

fun getDarkNoteAccentColor(colorCode: Long): Color {
    val pastel = Color(colorCode)
    return when (pastel) {
        PastelRed -> PastelRed
        PastelOrange -> PastelOrange
        PastelYellow -> PastelYellow
        PastelGreen -> PastelGreen
        PastelBlue -> PastelBlue
        PastelPurple -> PastelPurple
        PastelGrey -> Color(0xFFCFD8DC)
        else -> DarkOnSurface
    }
}

fun getNoteColor(colorCode: Long): Color {
    return Color(colorCode)
}
