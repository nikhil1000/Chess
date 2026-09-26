package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BoardState
import com.example.model.BoardPos
import com.example.model.ChessMove
import com.example.model.KillAnimationEvent
import com.example.model.PieceColor
import com.example.ui.components.KillAnimationEngine.drawKillAnimationSequence
import com.example.ui.theme.ArenaNeonBlue
import com.example.ui.theme.BoardBaseSlab
import com.example.ui.theme.BoardDarkMarbleBottom
import com.example.ui.theme.BoardDarkMarbleTop
import com.example.ui.theme.BoardFrameStone
import com.example.ui.theme.BoardLightMarbleBottom
import com.example.ui.theme.BoardLightMarbleTop
import com.example.ui.theme.CheckCrimson
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.SelectedAuraCyan
import com.example.ui.theme.SelectedAuraGold
import com.example.ui.theme.ValidMoveEmerald
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun ChessBoard3D(
    boardState: BoardState,
    selectedSquare: BoardPos?,
    validMoves: List<ChessMove>,
    hintMove: ChessMove?,
    activeKillEvent: KillAnimationEvent?,
    killAnimationProgress: Float,
    is3DPerspective: Boolean,
    isInteractionLocked: Boolean,
    onSquareTapped: (BoardPos) -> Unit,
    onMovePiece: (BoardPos, BoardPos) -> Unit = { _, to -> onSquareTapped(to) },
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Keep latest state references for pointerInput so gestures never drop or restart mid-click
    val currentBoardState by rememberUpdatedState(boardState)
    val currentValidMoves by rememberUpdatedState(validMoves)
    val currentIsLocked by rememberUpdatedState(isInteractionLocked)
    val currentOnSquareTapped by rememberUpdatedState(onSquareTapped)
    val currentOnMovePiece by rememberUpdatedState(onMovePiece)

    // Live drag state for smooth drag-and-drop piece movement
    var dragOriginSquare by remember { mutableStateOf<BoardPos?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
    var dragHoverSquare by remember { mutableStateOf<BoardPos?>(null) }

    // Continuous pulse for selected aura, valid move dots, and Check warning
    val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val selectedFloatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selectedFloat"
    )

    // Compute board-wide physical screen shake when a kill impact occurs!
    val maxShakePx = with(density) { 14.dp.toPx() }
    val (shakeX, shakeY, shakeRotZ) = remember(activeKillEvent, killAnimationProgress) {
        if (activeKillEvent != null) {
            KillAnimationEngine.computeBoardShake(killAnimationProgress, maxShakePx)
        } else {
            Triple(0f, 0f, 0f)
        }
    }

    val validMovesByDest = remember(validMoves) {
        validMoves.associateBy { it.to }
    }

    BoxWithConstraints(
        modifier = modifier.testTag("chess_board_container"),
        contentAlignment = Alignment.Center
    ) {
        val safeMaxW = if (maxWidth.value.isFinite() && maxWidth.value > 0f) maxWidth.value else 360f
        val safeMaxH = if (maxHeight.value.isFinite() && maxHeight.value > 0f) maxHeight.value else safeMaxW
        val boardContainerSize = min(safeMaxW, safeMaxH).dp

        // Exact symmetric grid inset (8.5% on each side -> 83% inner 8x8 square)
        val gridHorizontalPadding = boardContainerSize * 0.085f
        val gridTopPadding = boardContainerSize * 0.065f
        val gridBottomPadding = boardContainerSize * 0.105f

        Box(
            modifier = Modifier
                .size(boardContainerSize)
                .graphicsLayer {
                    rotationZ = shakeRotZ
                    translationX = shakeX
                    translationY = shakeY
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Monumental 3D Obsidian Pedestal Base Slab & Neon Side Conduits
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val frameInset = w * 0.028f
                val slabDepth = h * (if (is3DPerspective) 0.068f else 0.032f)

                // Deep ambient shadow under the stone plinth
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = Offset(frameInset * 0.35f, frameInset + slabDepth * 0.75f),
                    size = Size(w - frameInset * 0.7f, h - frameInset - slabDepth * 0.25f),
                    cornerRadius = CornerRadius(22f, 22f)
                )

                // 3D Carved Lower Stone Plinth (gives 3D perspective depth at the bottom of the board)
                val plinthPath = Path().apply {
                    moveTo(frameInset * 0.8f, h - slabDepth * 1.35f)
                    lineTo(w - frameInset * 0.8f, h - slabDepth * 1.35f)
                    lineTo(w - frameInset * 0.15f, h - slabDepth * 0.12f)
                    lineTo(frameInset * 0.15f, h - slabDepth * 0.12f)
                    close()
                }
                drawPath(
                    path = plinthPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            BoardBaseSlab,
                            Color(0xFF04070C)
                        )
                    )
                )
                drawPath(
                    path = plinthPath,
                    color = ArenaNeonBlue.copy(alpha = if (is3DPerspective) 0.55f else 0.25f),
                    style = Stroke(width = 2.5f)
                )

                // Outer Obsidian Frame
                val outerRectTop = frameInset * 0.35f
                val outerRectHeight = h - slabDepth * 1.05f - outerRectTop
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1F2B3E),
                            BoardFrameStone,
                            Color(0xFF0A0F18)
                        )
                    ),
                    topLeft = Offset(frameInset * 0.45f, outerRectTop),
                    size = Size(w - frameInset * 0.9f, outerRectHeight),
                    cornerRadius = CornerRadius(18f, 18f)
                )

                // 3D Inner Beveled Stone Ledge (visible when 3D Mode is enabled)
                if (is3DPerspective) {
                    drawRoundRect(
                        color = Color(0xFF334155).copy(alpha = 0.45f),
                        topLeft = Offset(frameInset * 0.65f, outerRectTop + 3f),
                        size = Size(w - frameInset * 1.3f, outerRectHeight - 6f),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 2f)
                    )
                }

                // Glowing Electric-Blue Neon Side Strips (matching the reference screenshot!)
                val neonStripAlpha = if (activeKillEvent != null) 0.98f else 0.80f
                val neonColor = if (boardState.status == com.example.model.GameStatusType.CHECK) {
                    CheckCrimson
                } else {
                    ArenaNeonBlue
                }

                // Left Neon Conduit
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonColor.copy(alpha = neonStripAlpha),
                            Color(0xFF60A5FA).copy(alpha = neonStripAlpha),
                            neonColor.copy(alpha = neonStripAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(frameInset * 0.55f, outerRectTop + 16f),
                    end = Offset(frameInset * 0.55f, outerRectTop + outerRectHeight - 16f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Right Neon Conduit
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonColor.copy(alpha = neonStripAlpha),
                            Color(0xFF60A5FA).copy(alpha = neonStripAlpha),
                            neonColor.copy(alpha = neonStripAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(w - frameInset * 0.55f, outerRectTop + 16f),
                    end = Offset(w - frameInset * 0.55f, outerRectTop + outerRectHeight - 16f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Inner Golden-Bronze Bezel around the 8x8 playing grid
                val gridLeft = w * 0.085f
                val gridTop = h * 0.065f
                val gridSize = w * 0.83f
                drawRoundRect(
                    color = RoyalGold.copy(alpha = 0.65f),
                    topLeft = Offset(gridLeft - 3f, gridTop - 3f),
                    size = Size(gridSize + 6f, gridSize + 6f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 3f)
                )

                // Coordinate Labels: Ranks 8..1 on Left, Files a..h on Bottom
                val coordStyle = TextStyle(
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                val tileSize = gridSize / 8f

                for (i in 0..7) {
                    val rankStr = (8 - i).toString()
                    val rankLayout = textMeasurer.measure(rankStr, coordStyle)
                    drawText(
                        textLayoutResult = rankLayout,
                        topLeft = Offset(
                            x = (gridLeft - rankLayout.size.width) * 0.54f,
                            y = gridTop + i * tileSize + (tileSize - rankLayout.size.height) * 0.5f
                        )
                    )

                    val fileStr = ('a' + i).toString()
                    val fileLayout = textMeasurer.measure(fileStr, coordStyle)
                    drawText(
                        textLayoutResult = fileLayout,
                        topLeft = Offset(
                            x = gridLeft + i * tileSize + (tileSize - fileLayout.size.width) * 0.5f,
                            y = gridTop + gridSize + (w * 0.036f - fileLayout.size.height * 0.5f)
                        )
                    )
                }
            }

            // 2. Interactive 8x8 Marble Grid + 3D Pieces + Glorified Kill Animation Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = gridHorizontalPadding,
                        end = gridHorizontalPadding,
                        top = gridTopPadding,
                        bottom = gridBottomPadding
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (currentIsLocked) return@awaitEachGesture

                            val gridW = size.width.toFloat()
                            val gridH = size.height.toFloat()
                            if (gridW <= 0f || gridH <= 0f) return@awaitEachGesture

                            val tileW = gridW / 8f
                            val tileH = gridH / 8f

                            val downCol = (down.position.x / tileW).toInt().coerceIn(0, 7)
                            val downRow = (down.position.y / tileH).toInt().coerceIn(0, 7)
                            val downPos = BoardPos(downRow, downCol)

                            val wasValidDestinationOnDown = currentValidMoves.any { it.to == downPos }
                            val pieceOnDown = currentBoardState.pieceAt(downPos)
                            val isFriendlyPieceOnDown = pieceOnDown != null && pieceOnDown.color == PieceColor.WHITE

                            // If pressing a friendly White piece (and not capturing/moving to it), select it immediately on press!
                            if (isFriendlyPieceOnDown && !wasValidDestinationOnDown) {
                                currentOnSquareTapped(downPos)
                                dragOriginSquare = downPos
                                dragHoverSquare = downPos
                                dragCurrentOffset = down.position
                            } else {
                                dragOriginSquare = null
                                dragHoverSquare = null
                                dragCurrentOffset = null
                            }

                            var lastPos = down.position
                            var isDragging = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                                if (change == null || !change.pressed) {
                                    break
                                }
                                lastPos = change.position
                                val moveDist = hypot(lastPos.x - down.position.x, lastPos.y - down.position.y)
                                if (moveDist > tileW * 0.18f && isFriendlyPieceOnDown) {
                                    isDragging = true
                                }
                                if (isDragging) {
                                    change.consume()
                                    dragCurrentOffset = lastPos
                                    val hCol = (lastPos.x / tileW).toInt().coerceIn(0, 7)
                                    val hRow = (lastPos.y / tileH).toInt().coerceIn(0, 7)
                                    dragHoverSquare = BoardPos(hRow, hCol)
                                }
                            }

                            dragOriginSquare = null
                            dragCurrentOffset = null
                            dragHoverSquare = null

                            val upCol = (lastPos.x / tileW).toInt().coerceIn(0, 7)
                            val upRow = (lastPos.y / tileH).toInt().coerceIn(0, 7)
                            val upPos = BoardPos(upRow, upCol)

                            if (upPos != downPos && isFriendlyPieceOnDown) {
                                // Completed a drag-and-drop from downPos to upPos!
                                currentOnMovePiece(downPos, upPos)
                            } else if (!isFriendlyPieceOnDown || wasValidDestinationOnDown) {
                                // Standard tap on a destination square or empty square
                                currentOnSquareTapped(upPos)
                            }
                        }
                    }
                    .testTag("chess_grid_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tileW = size.width / 8f
                    val tileH = size.height / 8f
                    val tileSize = min(tileW, tileH)

                    // Pass 1: Draw 64 Marbled Squares + Visual Anchors (Last Move, Selected, Check, Valid Moves)
                    for (r in 0..7) {
                        for (c in 0..7) {
                            val pos = BoardPos(r, c)
                            val topLeft = Offset(c * tileW, r * tileH)
                            val isLight = pos.isLightSquare

                            // Base luxury marble gradient
                            val tileBrush = if (isLight) {
                                Brush.linearGradient(
                                    colors = listOf(BoardLightMarbleTop, BoardLightMarbleBottom),
                                    start = topLeft,
                                    end = Offset(topLeft.x + tileW, topLeft.y + tileH)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(BoardDarkMarbleTop, BoardDarkMarbleBottom),
                                    start = topLeft,
                                    end = Offset(topLeft.x + tileW, topLeft.y + tileH)
                                )
                            }

                            drawRect(
                                brush = tileBrush,
                                topLeft = topLeft,
                                size = Size(tileW, tileH)
                            )

                            // Subtle procedural marble veining for AAA texture richness
                            drawMarbleVeins(
                                r = r,
                                c = c,
                                topLeft = topLeft,
                                tileSize = tileSize,
                                isLight = isLight
                            )

                            // Last move highlight
                            val isLastMoveFrom = boardState.lastMove?.from == pos
                            val isLastMoveTo = boardState.lastMove?.to == pos
                            if (isLastMoveFrom || isLastMoveTo) {
                                drawRect(
                                    color = RoyalGold.copy(alpha = if (isLastMoveTo) 0.28f else 0.16f),
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH)
                                )
                            }

                            // Drag hover target highlight
                            if (dragHoverSquare == pos && validMovesByDest.containsKey(pos)) {
                                drawRect(
                                    color = ValidMoveEmerald.copy(alpha = 0.36f),
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH)
                                )
                            }

                            // Tactical Hint highlight
                            if (hintMove?.from == pos || hintMove?.to == pos) {
                                drawRect(
                                    color = ArenaNeonBlue.copy(alpha = 0.32f * pulseAlpha),
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH)
                                )
                                drawRect(
                                    color = ArenaNeonBlue,
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH),
                                    style = Stroke(width = tileSize * 0.06f)
                                )
                            }

                            // Selected Piece Glowing Aura Frame (matching the blue/gold glowing square in reference!)
                            if (selectedSquare == pos) {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            SelectedAuraGold.copy(alpha = 0.45f * pulseAlpha),
                                            SelectedAuraCyan.copy(alpha = 0.25f * pulseAlpha),
                                            Color.Transparent
                                        ),
                                        center = Offset(topLeft.x + tileW / 2f, topLeft.y + tileH / 2f),
                                        radius = tileSize * 0.75f
                                    ),
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH)
                                )
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(SelectedAuraCyan, SelectedAuraGold, SelectedAuraCyan),
                                        start = topLeft,
                                        end = Offset(topLeft.x + tileW, topLeft.y + tileH)
                                    ),
                                    topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
                                    size = Size(tileW - 4f, tileH - 4f),
                                    cornerRadius = CornerRadius(4f, 4f),
                                    style = Stroke(width = tileSize * 0.075f)
                                )
                            }

                            // Pulsing Red Warning Effect on the King's Square when in Check!
                            if (boardState.kingInCheckPos == pos) {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            CheckCrimson.copy(alpha = 0.78f * pulseAlpha),
                                            Color(0xFF991B1B).copy(alpha = 0.45f * pulseAlpha),
                                            Color.Transparent
                                        ),
                                        center = Offset(topLeft.x + tileW / 2f, topLeft.y + tileH / 2f),
                                        radius = tileSize * 0.78f
                                    ),
                                    topLeft = topLeft,
                                    size = Size(tileW, tileH)
                                )
                                drawRoundRect(
                                    color = CheckCrimson.copy(alpha = 0.95f),
                                    topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
                                    size = Size(tileW - 4f, tileH - 4f),
                                    cornerRadius = CornerRadius(4f, 4f),
                                    style = Stroke(width = tileSize * 0.085f)
                                )
                            }

                            // Subtle tile bevel grid line
                            drawRect(
                                color = Color.Black.copy(alpha = 0.18f),
                                topLeft = topLeft,
                                size = Size(tileW, tileH),
                                style = Stroke(width = 1f)
                            )
                        }
                    }

                    // Pass 2: Draw Soft Glowing Emerald Dots & Capture Reticles for Valid Moves
                    for ((destPos, move) in validMovesByDest) {
                        val cx = (destPos.col + 0.5f) * tileW
                        val cy = (destPos.row + 0.5f) * tileH
                        val center = Offset(cx, cy)

                        if (move.isCapture) {
                            // High-visibility pulsing emerald + gold target reticle around capturable enemy piece
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ValidMoveEmerald.copy(alpha = 0.48f * pulseAlpha),
                                        Color(0xFFF59E0B).copy(alpha = 0.25f * pulseAlpha),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = tileSize * 0.52f
                                ),
                                radius = tileSize * 0.52f,
                                center = center
                            )
                            drawCircle(
                                color = ValidMoveEmerald.copy(alpha = 0.92f),
                                radius = tileSize * 0.42f,
                                center = center,
                                style = Stroke(width = tileSize * 0.065f)
                            )
                            drawCircle(
                                color = ValidMoveEmerald,
                                radius = tileSize * 0.12f,
                                center = Offset(cx + tileSize * 0.30f, cy - tileSize * 0.30f)
                            )
                        } else {
                            // Soft glowing green dot (matching the green orbs in the reference image!)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF86EFAC).copy(alpha = 0.95f),
                                        ValidMoveEmerald.copy(alpha = 0.85f * pulseAlpha),
                                        ValidMoveEmerald.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = tileSize * 0.28f
                                ),
                                radius = tileSize * 0.28f,
                                center = center
                            )
                            drawCircle(
                                color = Color(0xFF4ADE80),
                                radius = tileSize * 0.13f,
                                center = center
                            )
                        }
                    }

                    // Pass 3: Draw All Static 3D Chess Pieces
                    for (r in 0..7) {
                        for (c in 0..7) {
                            val pos = BoardPos(r, c)
                            val piece = boardState.pieceAt(pos) ?: continue

                            // Skip drawing static attacker & victim while activeKillEvent is animating them
                            if (activeKillEvent != null) {
                                if (pos == activeKillEvent.from || pos == activeKillEvent.to) {
                                    continue
                                }
                            }

                            // If user is actively dragging this piece, draw a subtle ghost on its origin square
                            val isBeingDragged = dragOriginSquare == pos && dragCurrentOffset != null

                            val isSelected = selectedSquare == pos
                            val isInCheck = boardState.kingInCheckPos == pos
                            val floatY = if (isSelected && !isBeingDragged) selectedFloatOffset else 0f
                            val pieceCenter = Offset(
                                x = (c + 0.5f) * tileW,
                                y = (r + 0.5f) * tileH + floatY
                            )

                            draw3DChessPiece(
                                type = piece.type,
                                color = piece.color,
                                center = pieceCenter,
                                pieceSize = tileSize * 0.88f,
                                isSelected = isSelected,
                                isInCheck = isInCheck,
                                elevationScale = if (isSelected && !isBeingDragged) 1.10f else 1.0f,
                                alpha = if (isBeingDragged) 0.35f else 1.0f
                            )
                        }
                    }

                    // Pass 3.5: Draw Live Dragged 3D Piece under finger/cursor
                    val draggedOrigin = dragOriginSquare
                    val draggedPoint = dragCurrentOffset
                    if (draggedOrigin != null && draggedPoint != null) {
                        val draggedPiece = boardState.pieceAt(draggedOrigin)
                        if (draggedPiece != null) {
                            draw3DChessPiece(
                                type = draggedPiece.type,
                                color = draggedPiece.color,
                                center = Offset(draggedPoint.x, draggedPoint.y - tileSize * 0.18f),
                                pieceSize = tileSize * 0.92f,
                                isSelected = true,
                                elevationScale = 1.18f
                            )
                        }
                    }

                    // Pass 4: Glorified 3D Kill Animation Sequence Overlay!
                    if (activeKillEvent != null) {
                        drawKillAnimationSequence(
                            event = activeKillEvent,
                            progress = killAnimationProgress,
                            tileSize = tileSize
                        )
                    }
                }
            }

            // 3. Floating Combat Callout Banner during Kill Animation
            AnimatedVisibility(
                visible = activeKillEvent != null,
                enter = fadeIn(tween(140)) + scaleIn(tween(180), initialScale = 0.82f),
                exit = fadeOut(tween(220)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                activeKillEvent?.let { ev ->
                    Row(
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xEE0F172A),
                                        Color(0xEE1E293B),
                                        Color(0xEE0F172A)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(RoyalGold, Color(0xFFEF4444), RoyalGold)
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Kill Strike",
                            tint = RoyalGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ev.attacker.type.displayName.uppercase()} SHATTERS ${ev.victim.type.displayName.uppercase()}!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = CinzelFontFamily,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ev.moveNotation,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMarbleVeins(
    r: Int,
    c: Int,
    topLeft: Offset,
    tileSize: Float,
    isLight: Boolean
) {
    val seed = (r * 13 + c * 7) % 5
    val veinColor = if (isLight) {
        Color(0xFF9E8C70).copy(alpha = 0.22f)
    } else {
        Color(0xFF475569).copy(alpha = 0.16f)
    }

    val path = Path().apply {
        when (seed) {
            0 -> {
                moveTo(topLeft.x + tileSize * 0.15f, topLeft.y + tileSize * 0.85f)
                quadraticTo(
                    topLeft.x + tileSize * 0.55f,
                    topLeft.y + tileSize * 0.45f,
                    topLeft.x + tileSize * 0.88f,
                    topLeft.y + tileSize * 0.20f
                )
            }
            1 -> {
                moveTo(topLeft.x + tileSize * 0.10f, topLeft.y + tileSize * 0.30f)
                quadraticTo(
                    topLeft.x + tileSize * 0.45f,
                    topLeft.y + tileSize * 0.65f,
                    topLeft.x + tileSize * 0.90f,
                    topLeft.y + tileSize * 0.78f
                )
            }
            2 -> {
                moveTo(topLeft.x + tileSize * 0.25f, topLeft.y + tileSize * 0.12f)
                quadraticTo(
                    topLeft.x + tileSize * 0.60f,
                    topLeft.y + tileSize * 0.50f,
                    topLeft.x + tileSize * 0.75f,
                    topLeft.y + tileSize * 0.88f
                )
            }
            else -> {
                moveTo(topLeft.x + tileSize * 0.18f, topLeft.y + tileSize * 0.62f)
                lineTo(topLeft.x + tileSize * 0.78f, topLeft.y + tileSize * 0.28f)
            }
        }
    }
    drawPath(
        path = path,
        color = veinColor,
        style = Stroke(width = 1.2f, cap = StrokeCap.Round)
    )
}
