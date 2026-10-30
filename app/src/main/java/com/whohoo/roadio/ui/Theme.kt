package com.whohoo.roadio.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0B0F14)
val Panel = Color(0xFF141A21)
val PanelRaised = Color(0xFF1E2830)
val Amber = Color(0xFFFFB74D)
val Mist = Color(0xFF9FB0C0)
val Paper = Color(0xFFF2F5F8)

private val RoadioColors = darkColorScheme(
    primary = Amber,
    onPrimary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Panel,
    onSurface = Paper,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = Mist,
)

@Composable
fun RoadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RoadioColors, content = content)
}
