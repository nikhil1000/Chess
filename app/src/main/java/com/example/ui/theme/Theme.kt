package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ChessArenaColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ArenaObsidianBg,
    primaryContainer = ArenaCardElevated,
    onPrimaryContainer = RoyalGoldLight,
    secondary = ArenaNeonBlue,
    onSecondary = ArenaObsidianBg,
    secondaryContainer = ArenaCardSurface,
    onSecondaryContainer = TextPrimaryIvory,
    tertiary = ValidMoveEmerald,
    onTertiary = ArenaObsidianBg,
    background = ArenaObsidianBg,
    onBackground = TextPrimaryIvory,
    surface = ArenaCharcoalSurface,
    onSurface = TextPrimaryIvory,
    surfaceVariant = ArenaCardSurface,
    onSurfaceVariant = TextSecondarySlate,
    outline = ArenaBorderSubtle,
    error = CheckCrimson,
    onError = TextPrimaryIvory
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ChessArenaColorScheme,
        typography = Typography,
        content = content
    )
}
