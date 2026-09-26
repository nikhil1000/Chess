package com.example.engine

import com.example.model.AIDifficulty
import com.example.model.BoardPos
import com.example.model.ChessMove
import com.example.model.PieceColor
import com.example.model.PieceType
import kotlin.math.max
import kotlin.math.min

object ChessAI {

    // Positional Piece-Square Tables (from White's perspective; row 0 = rank 8, row 7 = rank 1)
    private val PAWN_PST = arrayOf(
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(5,  5, 10, 25, 25, 10,  5,  5),
        intArrayOf(0,  0,  0, 20, 20,  0,  0,  0),
        intArrayOf(5, -5,-10,  0,  0,-10, -5,  5),
        intArrayOf(5, 10, 10,-20,-20, 10, 10,  5),
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0)
    )

    private val KNIGHT_PST = arrayOf(
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50),
        intArrayOf(-40,-20,  0,  0,  0,  0,-20,-40),
        intArrayOf(-30,  0, 10, 15, 15, 10,  0,-30),
        intArrayOf(-30,  5, 15, 20, 20, 15,  5,-30),
        intArrayOf(-30,  0, 15, 20, 20, 15,  0,-30),
        intArrayOf(-30,  5, 10, 15, 15, 10,  5,-30),
        intArrayOf(-40,-20,  0,  5,  5,  0,-20,-40),
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50)
    )

    private val BISHOP_PST = arrayOf(
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5, 10, 10,  5,  0,-10),
        intArrayOf(-10,  5,  5, 10, 10,  5,  5,-10),
        intArrayOf(-10,  0, 10, 10, 10, 10,  0,-10),
        intArrayOf(-10, 10, 10, 10, 10, 10, 10,-10),
        intArrayOf(-10,  5,  0,  0,  0,  0,  5,-10),
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20)
    )

    private val ROOK_PST = arrayOf(
        intArrayOf(  0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(  5, 10, 10, 10, 10, 10, 10,  5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(  0,  0,  0,  5,  5,  0,  0,  0)
    )

    private val QUEEN_PST = arrayOf(
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5,  5,  5,  5,  0,-10),
        intArrayOf( -5,  0,  5,  5,  5,  5,  0, -5),
        intArrayOf(  0,  0,  5,  5,  5,  5,  0, -5),
        intArrayOf(-10,  5,  5,  5,  5,  5,  0,-10),
        intArrayOf(-10,  0,  5,  0,  0,  0,  0,-10),
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20)
    )

    private val KING_PST = arrayOf(
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-20,-30,-30,-40,-40,-30,-30,-20),
        intArrayOf(-10,-20,-20,-20,-20,-20,-20,-10),
        intArrayOf( 20, 20,  0,  0,  0,  0, 20, 20),
        intArrayOf( 20, 30, 10,  0,  0, 10, 30, 20)
    )

    /**
     * Finds the best move for [aiColor] using Minimax with Alpha-Beta Pruning.
     */
    fun findBestMove(
        state: BoardState,
        difficulty: AIDifficulty,
        aiColor: PieceColor = state.turn
    ): ChessMove? {
        val legalMoves = ChessEngine.getAllLegalMoves(state, aiColor)
        if (legalMoves.isEmpty()) return null
        if (legalMoves.size == 1) return legalMoves.first()

        val orderedMoves = orderMoves(legalMoves)
        val depth = difficulty.searchDepth
        val usePositionalTables = difficulty != AIDifficulty.NOVICE

        var bestMove: ChessMove = orderedMoves.first()
        var alpha = -1_000_000
        var beta = 1_000_000

        if (aiColor == PieceColor.WHITE) {
            var maxEval = -1_000_000
            for (move in orderedMoves) {
                val nextGrid = ChessEngine.applyMoveToGridOnly(state.grid, move)
                val nextState = state.copy(
                    grid = nextGrid,
                    turn = PieceColor.BLACK,
                    enPassantTarget = computeEnPassant(move)
                )
                val eval = minimaxAlphaBeta(
                    state = nextState,
                    depth = depth - 1,
                    alpha = alpha,
                    beta = beta,
                    maximizingPlayer = false,
                    usePositionalTables = usePositionalTables
                )
                // Slight bonus for aggressive captures so the player gets exciting combat moments
                val tacticalBonus = if (move.isCapture) 8 else 0
                val totalScore = eval + tacticalBonus
                if (totalScore > maxEval) {
                    maxEval = totalScore
                    bestMove = move
                }
                alpha = max(alpha, totalScore)
            }
        } else {
            var minEval = 1_000_000
            for (move in orderedMoves) {
                val nextGrid = ChessEngine.applyMoveToGridOnly(state.grid, move)
                val nextState = state.copy(
                    grid = nextGrid,
                    turn = PieceColor.WHITE,
                    enPassantTarget = computeEnPassant(move)
                )
                val eval = minimaxAlphaBeta(
                    state = nextState,
                    depth = depth - 1,
                    alpha = alpha,
                    beta = beta,
                    maximizingPlayer = true,
                    usePositionalTables = usePositionalTables
                )
                val tacticalBonus = if (move.isCapture) -8 else 0
                val totalScore = eval + tacticalBonus
                if (totalScore < minEval) {
                    minEval = totalScore
                    bestMove = move
                }
                beta = min(beta, totalScore)
            }
        }

        return bestMove
    }

    private fun minimaxAlphaBeta(
        state: BoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        usePositionalTables: Boolean
    ): Int {
        if (depth <= 0) {
            return evaluateBoard(state.grid, usePositionalTables)
        }

        val currentColor = if (maximizingPlayer) PieceColor.WHITE else PieceColor.BLACK
        val legalMoves = ChessEngine.getAllLegalMoves(state, currentColor)

        if (legalMoves.isEmpty()) {
            val inCheck = ChessEngine.isKingInCheck(state.grid, currentColor)
            return if (inCheck) {
                // Prefer faster checkmates
                if (maximizingPlayer) -900_000 - (depth * 100) else 900_000 + (depth * 100)
            } else {
                0 // Stalemate
            }
        }

        val ordered = orderMoves(legalMoves)
        var currentAlpha = alpha
        var currentBeta = beta

        if (maximizingPlayer) {
            var maxEval = -1_000_000
            for (move in ordered) {
                val nextGrid = ChessEngine.applyMoveToGridOnly(state.grid, move)
                val nextState = state.copy(
                    grid = nextGrid,
                    turn = PieceColor.BLACK,
                    enPassantTarget = computeEnPassant(move)
                )
                val eval = minimaxAlphaBeta(
                    state = nextState,
                    depth = depth - 1,
                    alpha = currentAlpha,
                    beta = currentBeta,
                    maximizingPlayer = false,
                    usePositionalTables = usePositionalTables
                )
                maxEval = max(maxEval, eval)
                currentAlpha = max(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break // Beta cut-off
            }
            return maxEval
        } else {
            var minEval = 1_000_000
            for (move in ordered) {
                val nextGrid = ChessEngine.applyMoveToGridOnly(state.grid, move)
                val nextState = state.copy(
                    grid = nextGrid,
                    turn = PieceColor.WHITE,
                    enPassantTarget = computeEnPassant(move)
                )
                val eval = minimaxAlphaBeta(
                    state = nextState,
                    depth = depth - 1,
                    alpha = currentAlpha,
                    beta = currentBeta,
                    maximizingPlayer = true,
                    usePositionalTables = usePositionalTables
                )
                minEval = min(minEval, eval)
                currentBeta = min(currentBeta, eval)
                if (currentBeta <= currentAlpha) break // Alpha cut-off
            }
            return minEval
        }
    }

    /**
     * Evaluates the board from White's perspective (Positive = White advantage, Negative = Black advantage).
     */
    fun evaluateBoard(
        grid: List<List<com.example.model.ChessPiece?>>,
        usePositionalTables: Boolean = true
    ): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = grid[r][c] ?: continue
                val baseVal = piece.type.baseValue
                val pstBonus = if (usePositionalTables) {
                    val tableRow = if (piece.color == PieceColor.WHITE) r else (7 - r)
                    when (piece.type) {
                        PieceType.PAWN -> PAWN_PST[tableRow][c]
                        PieceType.KNIGHT -> KNIGHT_PST[tableRow][c]
                        PieceType.BISHOP -> BISHOP_PST[tableRow][c]
                        PieceType.ROOK -> ROOK_PST[tableRow][c]
                        PieceType.QUEEN -> QUEEN_PST[tableRow][c]
                        PieceType.KING -> KING_PST[tableRow][c]
                    }
                } else {
                    0
                }
                val pieceScore = baseVal + pstBonus
                if (piece.color == PieceColor.WHITE) {
                    score += pieceScore
                } else {
                    score -= pieceScore
                }
            }
        }
        return score
    }

    /**
     * MVV-LVA (Most Valuable Victim - Least Valuable Attacker) move ordering
     * for maximum Alpha-Beta pruning speed.
     */
    private fun orderMoves(moves: List<ChessMove>): List<ChessMove> {
        return moves.sortedByDescending { move ->
            var priority = 0
            if (move.capturedPiece != null) {
                priority += 10 * move.capturedPiece.type.baseValue - (move.piece.type.baseValue / 10)
            }
            if (move.isPromotion) {
                priority += (move.promotionType?.baseValue ?: 900)
            }
            if (move.isCastlingKingside || move.isCastlingQueenside) {
                priority += 60
            }
            // Center control bonus
            if (move.to.row in 3..4 && move.to.col in 3..4) {
                priority += 15
            }
            priority
        }
    }

    private fun computeEnPassant(move: ChessMove): BoardPos? {
        return if (
            move.piece.type == PieceType.PAWN &&
            kotlin.math.abs(move.to.row - move.from.row) == 2
        ) {
            BoardPos((move.from.row + move.to.row) / 2, move.from.col)
        } else {
            null
        }
    }
}
