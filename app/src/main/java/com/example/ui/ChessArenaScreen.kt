package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.MatchRecordEntity
import com.example.model.AIDifficulty
import com.example.model.BoardPos
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.ui.components.ArenaTopHeaderBar
import com.example.ui.components.CapturedPieceAnimationShowcaseCard
import com.example.ui.components.ChessBoard3D
import com.example.ui.components.CompactMobileStatusAndControlsBar
import com.example.ui.components.GameControlsCard
import com.example.ui.components.GameStatusCard
import com.example.ui.components.GoldenCrownIcon
import com.example.ui.components.MatchArchivesModal
import com.example.ui.components.MoveHistoryCard
import com.example.ui.components.PawnPromotionModal
import com.example.ui.components.PlayerGraveyardCard
import com.example.ui.theme.ArenaBorderSubtle
import com.example.ui.theme.ArenaObsidianBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextSecondarySlate

@Composable
fun ChessArenaScreen(
    viewModel: ChessArenaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val matchRecords by viewModel.matchRecords.collectAsStateWithLifecycle()

    ChessArenaContent(
        uiState = uiState,
        matchRecords = matchRecords,
        onSquareTapped = viewModel::onSquareTapped,
        onMovePiece = viewModel::onMovePiece,
        onToggle3D = viewModel::toggle3DPerspective,
        onToggleSound = viewModel::toggleSound,
        onLoadActionScenario = viewModel::loadBattleClashScenario,
        onOpenArchives = { viewModel.setShowArchivesModal(true) },
        onCloseArchives = { viewModel.setShowArchivesModal(false) },
        onClearArchives = viewModel::clearAllArchives,
        onResetGame = viewModel::resetGame,
        onUndoMove = viewModel::undoLastTurn,
        onRequestHint = viewModel::requestTacticalHint,
        onDifficultyChanged = viewModel::setDifficulty,
        onTriggerKillShowcase = viewModel::triggerShowcaseKillAnimation,
        onSelectPromotionType = viewModel::onSelectPromotionType,
        onConfirmPromotion = viewModel::onConfirmPromotion,
        onDismissPromotion = viewModel::onDismissPromotion,
        modifier = modifier
    )
}

@Composable
fun ChessArenaContent(
    uiState: ChessArenaUiState,
    matchRecords: List<MatchRecordEntity>,
    onSquareTapped: (BoardPos) -> Unit,
    onMovePiece: (BoardPos, BoardPos) -> Unit,
    onToggle3D: () -> Unit,
    onToggleSound: () -> Unit,
    onLoadActionScenario: () -> Unit,
    onOpenArchives: () -> Unit,
    onCloseArchives: () -> Unit,
    onClearArchives: () -> Unit,
    onResetGame: () -> Unit,
    onUndoMove: () -> Unit,
    onRequestHint: () -> Unit,
    onDifficultyChanged: (AIDifficulty) -> Unit,
    onTriggerKillShowcase: () -> Unit,
    onSelectPromotionType: (PieceType) -> Unit,
    onConfirmPromotion: () -> Unit,
    onDismissPromotion: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = uiState.pendingPromotion != null || uiState.showArchivesModal) {
        if (uiState.pendingPromotion != null) {
            onDismissPromotion()
        } else if (uiState.showArchivesModal) {
            onCloseArchives()
        }
    }

    val whiteAdv = (uiState.whiteMaterialScore - uiState.blackMaterialScore).coerceAtLeast(0)
    val blackAdv = (uiState.blackMaterialScore - uiState.whiteMaterialScore).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArenaObsidianBg)
    ) {
        // 1. Bespoke Atmospheric Dark-Fantasy Castle & Obsidian Arena Background
        Image(
            painter = painterResource(id = R.drawable.img_arena_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Deep Obsidian Vignette Overlay so UI cards & 3D Chessboard pop with crystal clarity
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x88070A10),
                            Color(0xCC070A10),
                            Color(0xF205080D)
                        )
                    )
                )
        )

        // 2. Main Responsive Content (Wide 3-Column Arena vs Single-Screen Mobile Layout)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val isWideScreen = maxWidth >= 820.dp

            if (isWideScreen) {
                // Canonical 3-Column Desktop/Tablet/Foldable Arena Layout (matching the reference screenshot!)
                Column(modifier = Modifier.fillMaxSize()) {
                    ArenaTopHeaderBar(
                        is3DPerspective = uiState.is3DPerspective,
                        soundEnabled = uiState.soundEnabled,
                        onToggle3D = onToggle3D,
                        onToggleSound = onToggleSound,
                        onLoadActionScenario = onLoadActionScenario,
                        onOpenArchives = onOpenArchives,
                        showQuoteOnRight = true
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left Column: You (White) Graveyard + AI (Black) Graveyard + Bottom Quote
                        Column(
                            modifier = Modifier
                                .width(235.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PlayerGraveyardCard(
                                    title = "You",
                                    subtitleColorName = "White",
                                    isHumanPlayer = true,
                                    isActiveTurn = uiState.boardState.turn == PieceColor.WHITE && !uiState.isGameOver,
                                    clockFormatted = formatClockSeconds(uiState.whiteClockSeconds),
                                    capturedPieces = uiState.boardState.capturedByWhite,
                                    capturedPieceColor = PieceColor.BLACK,
                                    materialDelta = whiteAdv
                                )

                                HorizontalDivider(
                                    color = ArenaBorderSubtle.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                PlayerGraveyardCard(
                                    title = "AI",
                                    subtitleColorName = "Black",
                                    isHumanPlayer = false,
                                    isActiveTurn = uiState.boardState.turn == PieceColor.BLACK && !uiState.isGameOver,
                                    clockFormatted = formatClockSeconds(uiState.blackClockSeconds),
                                    capturedPieces = uiState.boardState.capturedByBlack,
                                    capturedPieceColor = PieceColor.WHITE,
                                    materialDelta = blackAdv
                                )
                            }

                            // Bottom-Left Atmospheric Quote from the Reference Screenshot
                            Column(
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
                            ) {
                                Text(
                                    text = "Strategy\nis the art of\nturning possibilities\ninto victories.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondarySlate,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GoldenCrownIcon(modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(1.dp)
                                            .background(RoyalGold.copy(alpha = 0.65f))
                                    )
                                }
                            }
                        }

                        // Center Column: 3D Chessboard Pedestal & Kill Animation Stage
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            ChessBoard3D(
                                boardState = uiState.boardState,
                                selectedSquare = uiState.selectedSquare,
                                validMoves = uiState.validMovesForSelected,
                                hintMove = uiState.hintMove,
                                activeKillEvent = uiState.activeKillEvent,
                                killAnimationProgress = uiState.killAnimationProgress,
                                is3DPerspective = uiState.is3DPerspective,
                                isInteractionLocked = uiState.isInteractionLocked,
                                onSquareTapped = onSquareTapped,
                                onMovePiece = onMovePiece,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Right Column: Game Controls + Game Status + Move History + Captured Piece Animation Showcase
                        Column(
                            modifier = Modifier
                                .width(275.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameControlsCard(
                                difficulty = uiState.difficulty,
                                canUndo = uiState.canUndo,
                                isInteractionLocked = uiState.isInteractionLocked,
                                onResetGame = onResetGame,
                                onUndoMove = onUndoMove,
                                onRequestHint = onRequestHint,
                                onDifficultyChanged = onDifficultyChanged
                            )

                            GameStatusCard(
                                turn = uiState.boardState.turn,
                                status = uiState.boardState.status,
                                isAIThinking = uiState.isAIThinking,
                                isKillAnimating = uiState.activeKillEvent != null,
                                winner = uiState.boardState.winner
                            )

                            MoveHistoryCard(
                                movePairs = uiState.movePairs,
                                status = uiState.boardState.status,
                                winner = uiState.boardState.winner
                            )

                            CapturedPieceAnimationShowcaseCard(
                                lastKillDescription = uiState.lastKillDescription,
                                onTriggerKillShowcase = onTriggerKillShowcase,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // Single-Screen Portrait Handheld Layout (No page-level scroll stealing board gestures!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ArenaTopHeaderBar(
                        is3DPerspective = uiState.is3DPerspective,
                        soundEnabled = uiState.soundEnabled,
                        onToggle3D = onToggle3D,
                        onToggleSound = onToggleSound,
                        onLoadActionScenario = onLoadActionScenario,
                        onOpenArchives = onOpenArchives,
                        showQuoteOnRight = false
                    )

                    // Side-by-side Compact Player ("You") and Opponent ("AI") Graveyard Cards
                    Row(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PlayerGraveyardCard(
                            title = "You",
                            subtitleColorName = "White",
                            isHumanPlayer = true,
                            isActiveTurn = uiState.boardState.turn == PieceColor.WHITE && !uiState.isGameOver,
                            clockFormatted = formatClockSeconds(uiState.whiteClockSeconds),
                            capturedPieces = uiState.boardState.capturedByWhite,
                            capturedPieceColor = PieceColor.BLACK,
                            materialDelta = whiteAdv,
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )

                        PlayerGraveyardCard(
                            title = "AI",
                            subtitleColorName = "Black",
                            isHumanPlayer = false,
                            isActiveTurn = uiState.boardState.turn == PieceColor.BLACK && !uiState.isGameOver,
                            clockFormatted = formatClockSeconds(uiState.blackClockSeconds),
                            capturedPieces = uiState.boardState.capturedByBlack,
                            capturedPieceColor = PieceColor.WHITE,
                            materialDelta = blackAdv,
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Central 3D Chessboard Stage (takes flexible middle space so board + HUD fit on 1 screen)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 540.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessBoard3D(
                            boardState = uiState.boardState,
                            selectedSquare = uiState.selectedSquare,
                            validMoves = uiState.validMovesForSelected,
                            hintMove = uiState.hintMove,
                            activeKillEvent = uiState.activeKillEvent,
                            killAnimationProgress = uiState.killAnimationProgress,
                            is3DPerspective = uiState.is3DPerspective,
                            isInteractionLocked = uiState.isInteractionLocked,
                            onSquareTapped = onSquareTapped,
                            onMovePiece = onMovePiece,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Compact Bottom Arena Deck: Status + Controls Bar & Side-by-Side Move History + Kill Showcase
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactMobileStatusAndControlsBar(
                            turn = uiState.boardState.turn,
                            status = uiState.boardState.status,
                            isAIThinking = uiState.isAIThinking,
                            isKillAnimating = uiState.activeKillEvent != null,
                            winner = uiState.boardState.winner,
                            difficulty = uiState.difficulty,
                            canUndo = uiState.canUndo,
                            isInteractionLocked = uiState.isInteractionLocked,
                            onResetGame = onResetGame,
                            onUndoMove = onUndoMove,
                            onRequestHint = onRequestHint,
                            onDifficultyChanged = onDifficultyChanged,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            MoveHistoryCard(
                                movePairs = uiState.movePairs,
                                status = uiState.boardState.status,
                                winner = uiState.boardState.winner,
                                compact = true,
                                modifier = Modifier.weight(1.15f)
                            )

                            CapturedPieceAnimationShowcaseCard(
                                lastKillDescription = uiState.lastKillDescription,
                                onTriggerKillShowcase = onTriggerKillShowcase,
                                compact = true,
                                modifier = Modifier.weight(0.85f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Pawn Promotion Modal Overlay
        uiState.pendingPromotion?.let { pending ->
            PawnPromotionModal(
                pendingPromotion = pending,
                onSelectType = onSelectPromotionType,
                onConfirm = onConfirmPromotion,
                onDismiss = onDismissPromotion
            )
        }

        // 4. Persistent Room Match Archives Modal Overlay
        if (uiState.showArchivesModal) {
            MatchArchivesModal(
                records = matchRecords,
                onClearAll = onClearArchives,
                onDismiss = onCloseArchives
            )
        }
    }
}

@Preview(name = "Chess Arena - Mobile Portrait", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun ChessArenaMobilePreview() {
    MyApplicationTheme {
        ChessArenaContent(
            uiState = ChessArenaUiState(),
            matchRecords = emptyList(),
            onSquareTapped = {},
            onMovePiece = { _, _ -> },
            onToggle3D = {},
            onToggleSound = {},
            onLoadActionScenario = {},
            onOpenArchives = {},
            onCloseArchives = {},
            onClearArchives = {},
            onResetGame = {},
            onUndoMove = {},
            onRequestHint = {},
            onDifficultyChanged = {},
            onTriggerKillShowcase = {},
            onSelectPromotionType = {},
            onConfirmPromotion = {},
            onDismissPromotion = {}
        )
    }
}

@Preview(name = "Chess Arena - Wide Desktop/Tablet", showBackground = true, widthDp = 1080, heightDp = 680)
@Composable
fun ChessArenaWidePreview() {
    MyApplicationTheme {
        ChessArenaContent(
            uiState = ChessArenaUiState(),
            matchRecords = emptyList(),
            onSquareTapped = {},
            onMovePiece = { _, _ -> },
            onToggle3D = {},
            onToggleSound = {},
            onLoadActionScenario = {},
            onOpenArchives = {},
            onCloseArchives = {},
            onClearArchives = {},
            onResetGame = {},
            onUndoMove = {},
            onRequestHint = {},
            onDifficultyChanged = {},
            onTriggerKillShowcase = {},
            onSelectPromotionType = {},
            onConfirmPromotion = {},
            onDismissPromotion = {}
        )
    }
}
