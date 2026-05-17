package com.strategy.note.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val PastelRed = Color(0xFFFFB3B3)
val PastelOrange = Color(0xFFFFD1B3)
val PastelYellow = Color(0xFFFFF0B3)
val PastelGreen = Color(0xFFD1FFB3)
val PastelBlue = Color(0xFFB3E0FF)
val PastelPurple = Color(0xFFE0B3FF)
val PastelGrey = Color(0xFFE2E2E2)

val NoteColors = listOf(
    PastelRed,
    PastelOrange,
    PastelYellow,
    PastelGreen,
    PastelBlue,
    PastelPurple,
    PastelGrey
)

fun getNoteColor(colorCode: Long): Color {
    return Color(colorCode)
}
