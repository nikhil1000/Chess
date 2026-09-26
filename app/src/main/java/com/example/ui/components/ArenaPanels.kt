package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AIDifficulty
import com.example.model.ChessPiece
import com.example.model.GameStatusType
import com.example.model.MovePair
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.ui.theme.ArenaBorderCyanGlow
import com.example.ui.theme.ArenaBorderSubtle
import com.example.ui.theme.ArenaCardElevated
import com.example.ui.theme.ArenaCardSurface
import com.example.ui.theme.ArenaNeonBlue
import com.example.ui.theme.ArenaPanelBg
import com.example.ui.theme.CheckCrimson
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryIvory
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.ValidMoveEmerald

@Composable
fun GoldenCrownIcon(
    modifier: Modifier = Modifier,
    tint: Color = RoyalGold
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.14f, h * 0.78f)
            lineTo(w * 0.08f, h * 0.30f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.30f)
            lineTo(w * 0.86f, h * 0.78f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(RoyalGoldLight, tint, RoyalGoldDark)
            )
        )
        drawCircle(color = RoyalGoldLight, radius = w * 0.06f, center = Offset(w * 0.08f, h * 0.24f))
        drawCircle(color = RoyalGoldLight, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.13f))
        drawCircle(color = RoyalGoldLight, radius = w * 0.06f, center = Offset(w * 0.92f, h * 0.24f))
    }
}

@Composable
fun ArenaTopHeaderBar(
    is3DPerspective: Boolean,
    soundEnabled: Boolean,
    onToggle3D: () -> Unit,
    onToggleSound: () -> Unit,
    onLoadActionScenario: () -> Unit,
    onOpenArchives: () -> Unit,
    showQuoteOnRight: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (showQuoteOnRight) 14.dp else 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Identity: Golden Crown + CHESS ARENA + Think · Move · Conquer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            GoldenCrownIcon(
                modifier = Modifier.size(if (showQuoteOnRight) 34.dp else 24.dp)
            )
            Spacer(modifier = Modifier.width(if (showQuoteOnRight) 10.dp else 6.dp))
            Column {
                Text(
                    text = "CHESS ARENA",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (showQuoteOnRight) 22.sp else 16.sp,
                        lineHeight = if (showQuoteOnRight) 26.sp else 19.sp,
                        brush = Brush.verticalGradient(
                            colors = listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Think · Move · Conquer",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondarySlate,
                        fontSize = if (showQuoteOnRight) 12.sp else 10.sp,
                        lineHeight = 12.sp,
                        letterSpacing = 0.8.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Quick Toolbar Pills (3D View, Battle Scenario, Archives, Audio)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showQuoteOnRight) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text(
                        text = "Good moves win games,",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondarySlate,
                            fontStyle = FontStyle.Italic
                        )
                    )
                    Text(
                        text = "great moves build legends.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RoyalGoldLight.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }

            // 3D / 2D Perspective Toggle Chip
            Surface(
                onClick = onToggle3D,
                shape = RoundedCornerShape(8.dp),
                color = if (is3DPerspective) ArenaCardElevated else ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (is3DPerspective) ArenaNeonBlue else ArenaBorderSubtle
                ),
                modifier = Modifier.testTag("toggle_3d_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = "Toggle 3D Perspective",
                        tint = if (is3DPerspective) ArenaNeonBlue else TextSecondarySlate,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (is3DPerspective) "3D" else "2D",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (is3DPerspective) TextPrimaryIvory else TextSecondarySlate,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Instant Battle Scenario Loader (loads Ruy Lopez midgame with immediate capture opportunities!)
            Surface(
                onClick = onLoadActionScenario,
                shape = RoundedCornerShape(8.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                modifier = Modifier.testTag("battle_scenario_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Load Battle Scenario",
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (showQuoteOnRight) "Battle Clash" else "Clash",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = RoyalGoldLight,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Match Archives Button
            IconButton(
                onClick = onOpenArchives,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArenaCardSurface)
                    .border(1.dp, ArenaBorderSubtle, RoundedCornerShape(8.dp))
                    .testTag("open_archives_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Match Archives",
                    tint = RoyalGoldLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Sound & Haptics Toggle Button
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArenaCardSurface)
                    .border(1.dp, ArenaBorderSubtle, RoundedCornerShape(8.dp))
                    .testTag("toggle_sound_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = if (soundEnabled) ArenaNeonBlue else TextMutedSlate,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerGraveyardCard(
    title: String,
    subtitleColorName: String,
    isHumanPlayer: Boolean,
    isActiveTurn: Boolean,
    clockFormatted: String,
    capturedPieces: List<ChessPiece>,
    capturedPieceColor: PieceColor,
    materialDelta: Int,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActiveTurn) ArenaNeonBlue else ArenaBorderSubtle,
        animationSpec = tween(350),
        label = "playerCardBorder"
    )

    val countsByType = remember(capturedPieces) {
        val map = mutableMapOf<PieceType, Int>()
        for (p in capturedPieces) {
            map[p.type] = (map[p.type] ?: 0) + 1
        }
        map
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 11.dp else 14.dp))
            .background(ArenaPanelBg)
            .border(1.2.dp, borderColor, RoundedCornerShape(if (compact) 11.dp else 14.dp))
            .padding(if (compact) 8.dp else 12.dp)
    ) {
        // Top Row: Avatar + Player Name & Color + Active Clock Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Avatar Box
                Box(
                    modifier = Modifier
                        .size(if (compact) 28.dp else 40.dp)
                        .clip(RoundedCornerShape(if (compact) 7.dp else 10.dp))
                        .background(
                            if (isHumanPlayer) {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1D4ED8), Color(0xFF0F172A))
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF090D16))
                                )
                            }
                        )
                        .border(
                            1.2.dp,
                            if (isActiveTurn) ArenaNeonBlue else ArenaBorderSubtle,
                            RoundedCornerShape(if (compact) 7.dp else 10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHumanPlayer) {
                        ChessPiece3DIcon(
                            type = PieceType.KING,
                            color = PieceColor.WHITE,
                            modifier = Modifier.size(if (compact) 22.dp else 30.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Opponent",
                            tint = Color(0xFFE2E8F0),
                            modifier = Modifier.size(if (compact) 16.dp else 22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (compact) 6.dp else 10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimaryIvory,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (compact) 13.sp else 15.sp
                            ),
                            maxLines = 1
                        )
                        if (materialDelta > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$materialDelta",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ValidMoveEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ValidMoveEmerald.copy(alpha = 0.16f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = subtitleColorName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondarySlate,
                            fontSize = if (compact) 10.sp else 12.sp,
                            lineHeight = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Live Turn Dot + Clock Pill (e.g. 14:32 / 15:00)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArenaCardSurface)
                    .border(
                        1.dp,
                        if (isActiveTurn) ValidMoveEmerald.copy(alpha = 0.65f) else ArenaBorderSubtle,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 3.dp else 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 6.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isActiveTurn) ValidMoveEmerald else TextPrimaryIvory.copy(alpha = 0.45f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = clockFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = if (compact) 11.sp else 12.sp,
                        color = TextPrimaryIvory,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

        // Captured Pieces Graveyard Box (5-slot figurine row + numeric counter underneath like the reference image!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ArenaCardSurface.copy(alpha = 0.85f))
                .border(1.dp, ArenaBorderSubtle.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 4.dp else 8.dp)
        ) {
            if (!compact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Captured Pieces",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondarySlate,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${capturedPieces.size} total",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalGoldLight.copy(alpha = 0.85f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (pieceType in PieceType.graveyardOrder) {
                    val count = countsByType[pieceType] ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        ChessPiece3DIcon(
                            type = pieceType,
                            color = capturedPieceColor,
                            modifier = Modifier
                                .size(if (compact) 20.dp else 26.dp)
                                .alpha(if (count > 0) 1f else 0.35f)
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = if (compact) 10.sp else 11.sp,
                                color = if (count > 0) TextPrimaryIvory else TextMutedSlate,
                                fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact combined Status + Controls bar for Portrait Phone layout so the entire Arena fits on 1 screen
 * without scrolling or stealing touch gestures from the 3D Chessboard!
 */
@Composable
fun CompactMobileStatusAndControlsBar(
    turn: PieceColor,
    status: GameStatusType,
    isAIThinking: Boolean,
    isKillAnimating: Boolean,
    winner: PieceColor?,
    difficulty: AIDifficulty,
    canUndo: Boolean,
    isInteractionLocked: Boolean,
    onResetGame: () -> Unit,
    onUndoMove: () -> Unit,
    onRequestHint: () -> Unit,
    onDifficultyChanged: (AIDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val isWarning = status == GameStatusType.CHECK || status == GameStatusType.CHECKMATE

    val headlineText = when {
        isKillAnimating -> "Combat Strike!"
        status == GameStatusType.CHECKMATE -> if (winner == PieceColor.WHITE) "Victory! White Wins" else "Defeat! AI Wins"
        status == GameStatusType.STALEMATE -> "Draw by Stalemate"
        status == GameStatusType.TIMEOUT -> if (winner == PieceColor.WHITE) "White Wins on Time" else "AI Wins on Time"
        status == GameStatusType.CHECK -> "CHECK! Defend King"
        isAIThinking -> "AI Thinking..."
        turn == PieceColor.WHITE -> "White to move"
        else -> "Black to move"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ArenaPanelBg)
            .border(
                1.dp,
                if (isWarning) CheckCrimson else ArenaBorderCyanGlow.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("game_status_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                GoldenCrownIcon(
                    modifier = Modifier.size(16.dp),
                    tint = if (isWarning) CheckCrimson else RoyalGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isWarning) Color(0xFFFCA5A5) else TextPrimaryIvory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Difficulty Selector Pill
            Box {
                Surface(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = ArenaCardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
                    modifier = Modifier.testTag("difficulty_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Difficulty",
                            tint = ArenaNeonBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = difficulty.shortTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimaryIvory,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Difficulty",
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .background(ArenaCardElevated)
                        .border(1.dp, ArenaNeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    AIDifficulty.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (option == difficulty) RoyalGoldLight else TextPrimaryIvory
                                    )
                                )
                            },
                            onClick = {
                                onDifficultyChanged(option)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("difficulty_option_${option.name.lowercase()}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons Row: Reset Game | Undo Move | Hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                onClick = onResetGame,
                shape = RoundedCornerShape(8.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .testTag("reset_game_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Game",
                        tint = TextPrimaryIvory,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimaryIvory,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }
            }

            Surface(
                onClick = onUndoMove,
                enabled = canUndo && !isInteractionLocked,
                shape = RoundedCornerShape(8.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (canUndo && !isInteractionLocked) 1f else 0.45f)
                    .testTag("undo_move_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Move",
                        tint = TextPrimaryIvory,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Undo",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimaryIvory,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }
            }

            Surface(
                onClick = onRequestHint,
                enabled = !isInteractionLocked,
                shape = RoundedCornerShape(8.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (!isInteractionLocked) 1f else 0.45f)
                    .testTag("hint_move_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Tactical Hint",
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hint",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun GameControlsCard(
    difficulty: AIDifficulty,
    canUndo: Boolean,
    isInteractionLocked: Boolean,
    onResetGame: () -> Unit,
    onUndoMove: () -> Unit,
    onRequestHint: () -> Unit,
    onDifficultyChanged: (AIDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ArenaPanelBg)
            .border(1.2.dp, ArenaBorderCyanGlow.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "Game Controls",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimaryIvory,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Reset Game Button
        Surface(
            onClick = onResetGame,
            shape = RoundedCornerShape(10.dp),
            color = ArenaCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_game_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Game",
                    tint = TextPrimaryIvory,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Reset Game",
                    style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryIvory)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Undo Move & Tactical Hint Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onUndoMove,
                enabled = canUndo && !isInteractionLocked,
                shape = RoundedCornerShape(10.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (canUndo && !isInteractionLocked) 1f else 0.45f)
                    .testTag("undo_move_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Move",
                        tint = TextPrimaryIvory,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Undo Move",
                        style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryIvory),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onRequestHint,
                enabled = !isInteractionLocked,
                shape = RoundedCornerShape(10.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                modifier = Modifier
                    .alpha(if (!isInteractionLocked) 1f else 0.45f)
                    .testTag("hint_move_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Tactical Hint",
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hint",
                        style = MaterialTheme.typography.labelLarge.copy(color = RoyalGoldLight)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Difficulty Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "AI Difficulty",
                tint = ArenaNeonBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondarySlate,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Difficulty Dropdown Selector (matching "Master (3-4 depth) v" in reference image!)
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { dropdownExpanded = true },
                shape = RoundedCornerShape(10.dp),
                color = ArenaCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArenaBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("difficulty_selector")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = difficulty.label,
                        style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryIvory)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Difficulty",
                        tint = TextSecondarySlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier
                    .background(ArenaCardElevated)
                    .border(1.dp, ArenaNeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                AIDifficulty.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (option == difficulty) RoyalGoldLight else TextPrimaryIvory
                                    )
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondarySlate,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        },
                        onClick = {
                            onDifficultyChanged(option)
                            dropdownExpanded = false
                        },
                        modifier = Modifier.testTag("difficulty_option_${option.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
fun GameStatusCard(
    turn: PieceColor,
    status: GameStatusType,
    isAIThinking: Boolean,
    isKillAnimating: Boolean,
    winner: PieceColor?,
    modifier: Modifier = Modifier
) {
    val isWarning = status == GameStatusType.CHECK || status == GameStatusType.CHECKMATE
    val headlineText = when {
        isKillAnimating -> "Combat Execution!"
        status == GameStatusType.CHECKMATE -> {
            if (winner == PieceColor.WHITE) "Victory! White Wins" else "Defeat! AI Wins"
        }
        status == GameStatusType.STALEMATE -> "Draw by Stalemate"
        status == GameStatusType.DRAW_INSUFFICIENT_MATERIAL -> "Draw (Insufficient Material)"
        status == GameStatusType.DRAW_FIFTY_MOVE -> "Draw (50-Move Rule)"
        status == GameStatusType.TIMEOUT -> {
            if (winner == PieceColor.WHITE) "White Wins on Time" else "Black Wins on Time"
        }
        isAIThinking -> "AI is computing..."
        turn == PieceColor.WHITE -> "White to move"
        else -> "Black to move"
    }

    val subText = when {
        isKillAnimating -> "(Shattering captured piece)"
        status == GameStatusType.CHECK -> "WARNING: King is under attack!"
        status == GameStatusType.CHECKMATE -> "Checkmate on the board"
        isAIThinking -> "(Minimax Alpha-Beta Search)"
        turn == PieceColor.WHITE -> "(Your turn)"
        else -> "(AI Opponent's turn)"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ArenaPanelBg)
            .border(
                1.2.dp,
                if (isWarning) CheckCrimson else ArenaBorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
            .testTag("game_status_card")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoldenCrownIcon(modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Game Status",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryIvory,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ArenaCardSurface)
                    .border(
                        1.5.dp,
                        if (isWarning) CheckCrimson else RoyalGold,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                GoldenCrownIcon(
                    modifier = Modifier.size(18.dp),
                    tint = if (isWarning) CheckCrimson else RoyalGold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isWarning) Color(0xFFFCA5A5) else TextPrimaryIvory,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (status == GameStatusType.CHECK) CheckCrimson else TextSecondarySlate
                    )
                )
            }
        }
    }
}

@Composable
fun MoveHistoryCard(
    movePairs: List<MovePair>,
    status: GameStatusType,
    winner: PieceColor?,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(movePairs.size) {
        if (movePairs.isNotEmpty()) {
            listState.animateScrollToItem(movePairs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 12.dp else 14.dp))
            .background(ArenaPanelBg)
            .border(1.2.dp, ArenaBorderSubtle, RoundedCornerShape(if (compact) 12.dp else 14.dp))
            .padding(if (compact) 10.dp else 14.dp)
            .testTag("move_history_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Move History",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryIvory,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 13.sp else 15.sp
                )
            )
            Text(
                text = "PGN",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ArenaNeonBlue,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 6.dp else 8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.height(66.dp)
                    } else {
                        Modifier.heightIn(min = 120.dp, max = 210.dp)
                    }
                )
                .clip(RoundedCornerShape(8.dp))
                .background(ArenaCardSurface.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (movePairs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap a green dot or drag any White piece to move.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMutedSlate,
                            fontSize = if (compact) 11.sp else 12.sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(movePairs, key = { it.moveNumber }) { pair ->
                        val isLastRow = pair.moveNumber == movePairs.last().moveNumber
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isLastRow) ArenaCardElevated.copy(alpha = 0.8f) else Color.Transparent
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${pair.moveNumber}.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = TextSecondarySlate,
                                    fontSize = if (compact) 10.sp else 11.sp
                                ),
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = pair.whiteMove,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = if (compact) 10.sp else 11.sp,
                                    color = when {
                                        pair.isWhiteCheckOrMate -> Color(0xFFFCA5A5)
                                        pair.isWhiteCapture || (isLastRow && pair.blackMove == null) -> RoyalGoldLight
                                        else -> TextPrimaryIvory
                                    },
                                    fontWeight = if (pair.isWhiteCapture || isLastRow) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = pair.blackMove ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = if (compact) 10.sp else 11.sp,
                                    color = when {
                                        pair.isBlackCheckOrMate -> Color(0xFFFCA5A5)
                                        pair.isBlackCapture || (isLastRow && pair.blackMove != null) -> RoyalGoldLight
                                        else -> TextSecondarySlate
                                    },
                                    fontWeight = if (pair.isBlackCapture || isLastRow) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (!compact) {
            Spacer(modifier = Modifier.height(10.dp))

            val isGameOver = status == GameStatusType.CHECKMATE ||
                status == GameStatusType.STALEMATE ||
                status == GameStatusType.DRAW_FIFTY_MOVE ||
                status == GameStatusType.DRAW_INSUFFICIENT_MATERIAL ||
                status == GameStatusType.TIMEOUT

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ArenaCardSurface)
                    .border(
                        1.dp,
                        if (isGameOver) RoyalGold else ArenaBorderSubtle,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Game Over Trophy",
                        tint = RoyalGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Game Over",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryIvory,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGameOver) {
                        when (status) {
                            GameStatusType.CHECKMATE -> if (winner == PieceColor.WHITE) "1-0 · Checkmate! You conquered the AI." else "0-1 · Checkmate! AI claims the arena."
                            GameStatusType.TIMEOUT -> if (winner == PieceColor.WHITE) "1-0 · AI ran out of time." else "0-1 · Your clock expired."
                            else -> "½-½ · Match drawn ($status)."
                        }
                    } else {
                        "-"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isGameOver) RoyalGoldLight else TextPrimaryIvory,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (!isGameOver) {
                    Text(
                        text = "Checkmate, Stalemate or Resignation will appear here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMutedSlate,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CapturedPieceAnimationShowcaseCard(
    lastKillDescription: String?,
    onTriggerKillShowcase: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(if (compact) 104.dp else 112.dp)
            .clip(RoundedCornerShape(if (compact) 12.dp else 14.dp))
            .border(1.2.dp, RoyalGold.copy(alpha = 0.65f), RoundedCornerShape(if (compact) 12.dp else 14.dp))
            .clickable(onClick = onTriggerKillShowcase)
            .testTag("replay_kill_anim_button")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_capture_banner),
            contentDescription = "Captured Piece Animation Showcase",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Cinematic dark gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xAA070A10),
                            Color(0xF0070A10)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 28.dp else 34.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD0F172A))
                    .border(1.5.dp, RoyalGoldLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Kill Animation",
                    tint = RoyalGoldLight,
                    modifier = Modifier.size(if (compact) 16.dp else 20.dp)
                )
            }
            Spacer(modifier = Modifier.width(if (compact) 6.dp else 10.dp))
            Column {
                Text(
                    text = if (compact) "Kill Animation" else "Captured Piece Animation",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimaryIvory,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 12.sp else 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lastKillDescription ?: "Tap to unleash 3D Shatter Strike!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoyalGoldLight.copy(alpha = 0.9f),
                        fontSize = if (compact) 10.sp else 11.sp,
                        lineHeight = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
