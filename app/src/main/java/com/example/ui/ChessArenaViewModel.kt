package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ArenaDatabase
import com.example.data.MatchRecordEntity
import com.example.engine.BoardState
import com.example.engine.ChessAI
import com.example.engine.ChessEngine
import com.example.model.AIDifficulty
import com.example.model.BoardPos
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.GameStatusType
import com.example.model.KillAnimationEvent
import com.example.model.KillImpactStyle
import com.example.model.MovePair
import com.example.model.PendingPromotion
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.util.ArenaFeedbackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DEFAULT_INITIAL_BOARD = ChessEngine.createInitialState()
private val DEFAULT_INITIAL_FOCUS = BoardPos(6, 4) // e2 White Pawn pre-selected on launch
private val DEFAULT_INITIAL_MOVES = ChessEngine.getLegalMovesForSquare(DEFAULT_INITIAL_BOARD, DEFAULT_INITIAL_FOCUS)

data class ChessArenaUiState(
    val boardState: BoardState = DEFAULT_INITIAL_BOARD,
    val selectedSquare: BoardPos? = DEFAULT_INITIAL_FOCUS,
    val validMovesForSelected: List<ChessMove> = DEFAULT_INITIAL_MOVES,
    val hintMove: ChessMove? = null,
    val difficulty: AIDifficulty = AIDifficulty.MASTER,
    val is3DPerspective: Boolean = true,
    val soundEnabled: Boolean = true,
    val isAIThinking: Boolean = false,
    val activeKillEvent: KillAnimationEvent? = null,
    val killAnimationProgress: Float = 0f,
    val lastKillDescription: String? = null,
    val pendingPromotion: PendingPromotion? = null,
    val whiteClockSeconds: Int = 15 * 60,
    val blackClockSeconds: Int = 15 * 60,
    val canUndo: Boolean = false,
    val showArchivesModal: Boolean = false
) {
    val isInteractionLocked: Boolean
        get() = isAIThinking ||
            activeKillEvent != null ||
            pendingPromotion != null ||
            boardState.turn != PieceColor.WHITE ||
            isGameOver

    val isGameOver: Boolean
        get() = boardState.status == GameStatusType.CHECKMATE ||
            boardState.status == GameStatusType.STALEMATE ||
            boardState.status == GameStatusType.DRAW_FIFTY_MOVE ||
            boardState.status == GameStatusType.DRAW_INSUFFICIENT_MATERIAL ||
            boardState.status == GameStatusType.TIMEOUT ||
            boardState.status == GameStatusType.RESIGNED

    val movePairs: List<MovePair>
        get() {
            val moves = boardState.moveHistory
            val pairs = mutableListOf<MovePair>()
            var i = 0
            var moveNum = 1
            while (i < moves.size) {
                val wMove = moves[i]
                val bMove = if (i + 1 < moves.size) moves[i + 1] else null
                pairs.add(
                    MovePair(
                        moveNumber = moveNum++,
                        whiteMove = wMove.algebraicNotation,
                        blackMove = bMove?.algebraicNotation,
                        isWhiteCapture = wMove.isCapture,
                        isBlackCapture = bMove?.isCapture == true,
                        isWhiteCheckOrMate = wMove.algebraicNotation.endsWith("+") || wMove.algebraicNotation.endsWith("#"),
                        isBlackCheckOrMate = bMove?.algebraicNotation?.let { it.endsWith("+") || it.endsWith("#") } == true
                    )
                )
                i += 2
            }
            return pairs
        }

    val whiteMaterialScore: Int
        get() = boardState.capturedByWhite.sumOf { it.type.baseValue / 100 }

    val blackMaterialScore: Int
        get() = boardState.capturedByBlack.sumOf { it.type.baseValue / 100 }
}

class ChessArenaViewModel(application: Application) : AndroidViewModel(application) {

    private val matchDao = runCatching {
        ArenaDatabase.getInstance(application).matchRecordDao()
    }.getOrNull()

    private val feedback = ArenaFeedbackManager(application)

    private val _uiState = MutableStateFlow(ChessArenaUiState())
    val uiState: StateFlow<ChessArenaUiState> = _uiState.asStateFlow()

    val matchRecords: StateFlow<List<MatchRecordEntity>> = (
        matchDao?.getRecentMatches()?.catch { emit(emptyList()) }
            ?: MutableStateFlow(emptyList())
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stack of previous BoardStates for Undo Move
    private val undoStack = ArrayDeque<BoardState>()
    private var lastCapturedEventCache: KillAnimationEvent? = null
    private var aiTurnJob: Job? = null
    private var animationJob: Job? = null

    init {
        startChessClockTicker()
    }

    private fun startChessClockTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (current.isGameOver || current.activeKillEvent != null || current.pendingPromotion != null) {
                    continue
                }
                if (current.boardState.moveHistory.isEmpty()) {
                    // Start ticking after the first move is made
                    continue
                }
                if (current.boardState.turn == PieceColor.WHITE) {
                    val nextSec = (current.whiteClockSeconds - 1).coerceAtLeast(0)
                    if (nextSec == 0) {
                        handleTimeout(winner = PieceColor.BLACK)
                    } else {
                        _uiState.update { it.copy(whiteClockSeconds = nextSec) }
                    }
                } else {
                    val nextSec = (current.blackClockSeconds - 1).coerceAtLeast(0)
                    if (nextSec == 0) {
                        handleTimeout(winner = PieceColor.WHITE)
                    } else {
                        _uiState.update { it.copy(blackClockSeconds = nextSec) }
                    }
                }
            }
        }
    }

    private fun handleTimeout(winner: PieceColor) {
        feedback.onVictoryOrGameOver()
        _uiState.update { state ->
            val updatedBoard = state.boardState.copy(
                status = GameStatusType.TIMEOUT,
                winner = winner
            )
            state.copy(
                boardState = updatedBoard,
                selectedSquare = null,
                validMovesForSelected = emptyList(),
                isAIThinking = false
            )
        }
        persistMatchResultIfNeeded()
    }

    fun onSquareTapped(pos: BoardPos) {
        val state = _uiState.value
        if (state.isInteractionLocked) return

        val currentBoard = state.boardState
        val tappedPiece = currentBoard.pieceAt(pos)

        // 1. Check if the tapped square is a valid destination for the currently selected piece
        val matchingMove = state.validMovesForSelected.firstOrNull { it.to == pos }
        if (matchingMove != null) {
            if (matchingMove.isPromotion) {
                feedback.onPieceSelected()
                _uiState.update {
                    it.copy(
                        pendingPromotion = PendingPromotion(
                            from = matchingMove.from,
                            to = matchingMove.to,
                            pawn = matchingMove.piece,
                            capturedPiece = matchingMove.capturedPiece,
                            selectedType = PieceType.QUEEN
                        ),
                        hintMove = null
                    )
                }
                return
            }

            executePlayerMove(matchingMove)
            return
        }

        // 2. Otherwise, if the user tapped one of their own White pieces, select it and compute legal moves
        if (tappedPiece != null && tappedPiece.color == PieceColor.WHITE) {
            feedback.onPieceSelected()
            val legalMoves = ChessEngine.getLegalMovesForSquare(currentBoard, pos)
            _uiState.update {
                it.copy(
                    selectedSquare = pos,
                    validMovesForSelected = legalMoves,
                    hintMove = null
                )
            }
        } else {
            // Tapped empty or enemy square not in valid moves -> clear selection
            _uiState.update {
                it.copy(selectedSquare = null, validMovesForSelected = emptyList())
            }
        }
    }

    /**
     * Direct atomic move handler for drag-and-drop from [from] square to [to] square.
     */
    fun onMovePiece(from: BoardPos, to: BoardPos) {
        val state = _uiState.value
        if (state.isInteractionLocked) return
        val legalMoves = ChessEngine.getLegalMovesForSquare(state.boardState, from)
        val matchingMove = legalMoves.firstOrNull { it.to == to } ?: return

        if (matchingMove.isPromotion) {
            feedback.onPieceSelected()
            _uiState.update {
                it.copy(
                    selectedSquare = from,
                    validMovesForSelected = legalMoves,
                    pendingPromotion = PendingPromotion(
                        from = matchingMove.from,
                        to = matchingMove.to,
                        pawn = matchingMove.piece,
                        capturedPiece = matchingMove.capturedPiece,
                        selectedType = PieceType.QUEEN
                    ),
                    hintMove = null
                )
            }
            return
        }

        executePlayerMove(matchingMove)
    }

    fun onSelectPromotionType(type: PieceType) {
        feedback.onPieceSelected()
        _uiState.update { state ->
            val currentPromo = state.pendingPromotion ?: return@update state
            state.copy(pendingPromotion = currentPromo.copy(selectedType = type))
        }
    }

    fun onConfirmPromotion() {
        val state = _uiState.value
        val promo = state.pendingPromotion ?: return
        val legalFromSource = ChessEngine.getLegalMovesForSquare(state.boardState, promo.from)
        val matchingMove = legalFromSource.firstOrNull {
            it.from == promo.from && it.to == promo.to
        } ?: return

        val promotedMove = matchingMove.copy(
            isPromotion = true,
            promotionType = promo.selectedType
        )
        _uiState.update { it.copy(pendingPromotion = null) }
        executePlayerMove(promotedMove)
    }

    fun onDismissPromotion() {
        _uiState.update { it.copy(pendingPromotion = null) }
    }

    private fun executePlayerMove(move: ChessMove) {
        val beforeBoard = _uiState.value.boardState
        undoStack.addLast(beforeBoard)

        // Clear selection immediately
        _uiState.update {
            it.copy(
                selectedSquare = null,
                validMovesForSelected = emptyList(),
                hintMove = null,
                canUndo = undoStack.isNotEmpty()
            )
        }

        val nextBoard = ChessEngine.applyMove(beforeBoard, move)
        val finalizedMove = nextBoard.lastMove ?: move

        if (finalizedMove.isCapture && finalizedMove.capturedPiece != null) {
            runKillAnimationThenCommit(
                move = finalizedMove,
                victim = finalizedMove.capturedPiece,
                nextBoard = nextBoard,
                triggerAiAfter = !isTerminalStatus(nextBoard.status)
            )
        } else {
            feedback.onNormalMove()
            commitBoardUpdate(nextBoard, triggerAiAfter = !isTerminalStatus(nextBoard.status))
        }
    }

    private fun runKillAnimationThenCommit(
        move: ChessMove,
        victim: ChessPiece,
        nextBoard: BoardState,
        triggerAiAfter: Boolean
    ) {
        animationJob?.cancel()
        val victimSquare = move.capturedPos ?: move.to
        val killEvent = KillAnimationEvent(
            attacker = if (move.isPromotion && move.promotionType != null) {
                move.piece.copy(type = move.promotionType)
            } else {
                move.piece
            },
            victim = victim,
            from = move.from,
            to = move.to,
            victimPos = victimSquare,
            moveNotation = move.algebraicNotation,
            impactStyle = KillImpactStyle.SHATTER_OBSIDIAN,
            isReplayOrDemo = false
        )
        lastCapturedEventCache = killEvent

        val description = "${killEvent.attacker.color.displayName()} ${killEvent.attacker.type.displayName} shattered ${victim.color.displayName()} ${victim.type.displayName} (${move.algebraicNotation})"

        animationJob = viewModelScope.launch {
            feedback.onKillDashStart()
            _uiState.update {
                it.copy(
                    activeKillEvent = killEvent,
                    killAnimationProgress = 0f,
                    lastKillDescription = description
                )
            }

            var impactSoundPlayed = false
            val totalFrames = 52
            val frameDelayMs = 16L // ~830ms high-impact 60fps combat sequence
            for (frame in 1..totalFrames) {
                val progress = frame.toFloat() / totalFrames.toFloat()
                if (!impactSoundPlayed && progress >= 0.32f) {
                    impactSoundPlayed = true
                    feedback.onKillImpactExplosion()
                }
                _uiState.update { it.copy(killAnimationProgress = progress) }
                delay(frameDelayMs)
            }

            _uiState.update {
                it.copy(
                    activeKillEvent = null,
                    killAnimationProgress = 0f
                )
            }

            commitBoardUpdate(nextBoard, triggerAiAfter = triggerAiAfter)
        }
    }

    private fun commitBoardUpdate(nextBoard: BoardState, triggerAiAfter: Boolean) {
        if (nextBoard.status == GameStatusType.CHECK) {
            feedback.onKingInCheck()
        } else if (isTerminalStatus(nextBoard.status)) {
            feedback.onVictoryOrGameOver()
        }

        _uiState.update {
            it.copy(
                boardState = nextBoard,
                canUndo = undoStack.isNotEmpty()
            )
        }

        if (isTerminalStatus(nextBoard.status)) {
            persistMatchResultIfNeeded()
            return
        }

        if (triggerAiAfter && nextBoard.turn == PieceColor.BLACK) {
            triggerAiTurn()
        }
    }

    private fun triggerAiTurn() {
        aiTurnJob?.cancel()
        aiTurnJob = viewModelScope.launch {
            _uiState.update { it.copy(isAIThinking = true) }
            delay(280L)

            val currentState = _uiState.value
            val boardBeforeAi = currentState.boardState
            val difficulty = currentState.difficulty

            val chosenMove = withContext(Dispatchers.Default) {
                ChessAI.findBestMove(
                    state = boardBeforeAi,
                    difficulty = difficulty,
                    aiColor = PieceColor.BLACK
                )
            }

            _uiState.update { it.copy(isAIThinking = false) }

            if (chosenMove == null) return@launch

            val nextBoard = ChessEngine.applyMove(boardBeforeAi, chosenMove)
            val finalizedMove = nextBoard.lastMove ?: chosenMove

            if (finalizedMove.isCapture && finalizedMove.capturedPiece != null) {
                runKillAnimationThenCommit(
                    move = finalizedMove,
                    victim = finalizedMove.capturedPiece,
                    nextBoard = nextBoard,
                    triggerAiAfter = false
                )
            } else {
                feedback.onNormalMove()
                commitBoardUpdate(nextBoard, triggerAiAfter = false)
            }
        }
    }

    /**
     * Replays the most recent capture animation or showcases an epic Knight-shatters-piece combat strike
     * directly on the board when the user clicks the "Captured Piece Animation" showcase card!
     */
    fun triggerShowcaseKillAnimation() {
        val current = _uiState.value
        if (current.activeKillEvent != null || current.isAIThinking) return

        val demoEvent = lastCapturedEventCache?.copy(
            eventId = System.currentTimeMillis(),
            isReplayOrDemo = true
        ) ?: KillAnimationEvent(
            eventId = System.currentTimeMillis(),
            attacker = ChessPiece(id = 991, type = PieceType.KNIGHT, color = PieceColor.WHITE),
            victim = ChessPiece(id = 992, type = PieceType.BISHOP, color = PieceColor.BLACK),
            from = BoardPos(3, 3), // d5
            to = BoardPos(4, 5),   // f4 (matches the golden Knight strike in the reference screenshot!)
            victimPos = BoardPos(4, 5),
            moveNotation = "Nxf4!",
            impactStyle = KillImpactStyle.SHATTER_OBSIDIAN,
            isReplayOrDemo = true
        )

        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            feedback.onKillDashStart()
            _uiState.update {
                it.copy(
                    activeKillEvent = demoEvent,
                    killAnimationProgress = 0f,
                    lastKillDescription = "Replaying: ${demoEvent.attacker.type.displayName} shatters ${demoEvent.victim.type.displayName} (${demoEvent.moveNotation})"
                )
            }

            var impactFired = false
            val totalFrames = 54
            for (frame in 1..totalFrames) {
                val p = frame.toFloat() / totalFrames.toFloat()
                if (!impactFired && p >= 0.32f) {
                    impactFired = true
                    feedback.onKillImpactExplosion()
                }
                _uiState.update { it.copy(killAnimationProgress = p) }
                delay(16L)
            }

            _uiState.update {
                it.copy(
                    activeKillEvent = null,
                    killAnimationProgress = 0f
                )
            }
        }
    }

    fun resetGame() {
        aiTurnJob?.cancel()
        animationJob?.cancel()
        undoStack.clear()
        feedback.onPieceSelected()
        val initialBoard = ChessEngine.createInitialState()
        val initialFocus = BoardPos(6, 4)
        val initialMoves = ChessEngine.getLegalMovesForSquare(initialBoard, initialFocus)
        _uiState.update {
            it.copy(
                boardState = initialBoard,
                selectedSquare = initialFocus,
                validMovesForSelected = initialMoves,
                hintMove = null,
                isAIThinking = false,
                activeKillEvent = null,
                killAnimationProgress = 0f,
                pendingPromotion = null,
                whiteClockSeconds = 15 * 60,
                blackClockSeconds = 15 * 60,
                canUndo = false
            )
        }
    }

    /**
     * Loads the classic Ruy Lopez tactical clash from the reference screenshot (after 11...exd4),
     * giving the user immediate capture opportunities (12. cxd4, 12. Nxd4, or 12. Bxf6) right away!
     */
    fun loadBattleClashScenario() {
        aiTurnJob?.cancel()
        animationJob?.cancel()
        undoStack.clear()
        feedback.onPieceSelected()
        val tacticalBoard = ChessEngine.createTacticalArenaScenario()
        undoStack.addLast(ChessEngine.createInitialState())
        val focusSquare = BoardPos(5, 2) // c3 pawn poised to capture d4!
        val validMoves = ChessEngine.getLegalMovesForSquare(tacticalBoard, focusSquare)
        _uiState.update {
            it.copy(
                boardState = tacticalBoard,
                selectedSquare = focusSquare,
                validMovesForSelected = validMoves,
                hintMove = null,
                isAIThinking = false,
                activeKillEvent = null,
                killAnimationProgress = 0f,
                pendingPromotion = null,
                whiteClockSeconds = 14 * 60 + 32,
                blackClockSeconds = 15 * 60,
                canUndo = true
            )
        }
    }

    fun undoLastTurn() {
        val current = _uiState.value
        if (current.isInteractionLocked || undoStack.isEmpty()) return
        feedback.onPieceSelected()
        val restoredBoard = undoStack.removeLast()
        _uiState.update {
            it.copy(
                boardState = restoredBoard,
                selectedSquare = null,
                validMovesForSelected = emptyList(),
                hintMove = null,
                canUndo = undoStack.isNotEmpty()
            )
        }
    }

    fun requestTacticalHint() {
        val current = _uiState.value
        if (current.isInteractionLocked) return
        feedback.onPieceSelected()
        viewModelScope.launch {
            val bestPlayerMove = withContext(Dispatchers.Default) {
                ChessAI.findBestMove(
                    state = current.boardState,
                    difficulty = AIDifficulty.TACTICIAN,
                    aiColor = PieceColor.WHITE
                )
            }
            if (bestPlayerMove != null) {
                val legalForFrom = ChessEngine.getLegalMovesForSquare(current.boardState, bestPlayerMove.from)
                _uiState.update {
                    it.copy(
                        selectedSquare = bestPlayerMove.from,
                        validMovesForSelected = legalForFrom,
                        hintMove = bestPlayerMove
                    )
                }
            }
        }
    }

    fun setDifficulty(difficulty: AIDifficulty) {
        feedback.onPieceSelected()
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun toggle3DPerspective() {
        feedback.onPieceSelected()
        _uiState.update { it.copy(is3DPerspective = !it.is3DPerspective) }
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        feedback.soundEnabled = next
        feedback.hapticsEnabled = next
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun setShowArchivesModal(show: Boolean) {
        _uiState.update { it.copy(showArchivesModal = show) }
    }

    fun clearAllArchives() {
        viewModelScope.launch {
            runCatching { matchDao?.clearAllMatches() }
        }
    }

    private fun isTerminalStatus(status: GameStatusType): Boolean {
        return status == GameStatusType.CHECKMATE ||
            status == GameStatusType.STALEMATE ||
            status == GameStatusType.DRAW_FIFTY_MOVE ||
            status == GameStatusType.DRAW_INSUFFICIENT_MATERIAL ||
            status == GameStatusType.TIMEOUT ||
            status == GameStatusType.RESIGNED
    }

    private fun persistMatchResultIfNeeded() {
        val state = _uiState.value
        val board = state.boardState
        if (!isTerminalStatus(board.status)) return

        val winnerStr = board.winner?.name ?: "DRAW"
        val title = when (board.status) {
            GameStatusType.CHECKMATE -> if (board.winner == PieceColor.WHITE) "Victory by Checkmate" else "Defeat by Checkmate"
            GameStatusType.TIMEOUT -> if (board.winner == PieceColor.WHITE) "Victory on Time" else "Defeat on Time"
            GameStatusType.STALEMATE -> "Draw by Stalemate"
            else -> "Match Drawn"
        }

        val totalCaptures = board.capturedByWhite.size + board.capturedByBlack.size
        val pgnSummary = state.movePairs.takeLast(6).joinToString(" ") { pair ->
            "${pair.moveNumber}. ${pair.whiteMove} ${pair.blackMove ?: ""}".trim()
        }

        viewModelScope.launch {
            runCatching {
                matchDao?.insertMatch(
                    MatchRecordEntity(
                        difficultyLabel = state.difficulty.shortTitle,
                        resultTitle = title,
                        winnerColor = winnerStr,
                        totalMoves = board.moveHistory.size,
                        totalCaptures = totalCaptures,
                        pgnSummary = pgnSummary
                    )
                )
            }
        }
    }
}

fun formatClockSeconds(totalSeconds: Int): String {
    val mins = (totalSeconds / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(mins, secs)
}
