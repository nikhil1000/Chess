package com.example.model

import androidx.compose.runtime.Immutable

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
    fun displayName(): String = if (this == WHITE) "White" else "Black"
}

enum class PieceType(
    val displayName: String,
    val algebraicSymbol: String,
    val baseValue: Int
) {
    PAWN("Pawn", "", 100),
    KNIGHT("Knight", "N", 320),
    BISHOP("Bishop", "B", 330),
    ROOK("Rook", "R", 500),
    QUEEN("Queen", "Q", 900),
    KING("King", "K", 20000);

    companion object {
        val graveyardOrder = listOf(PAWN, KNIGHT, BISHOP, ROOK, QUEEN)
        val promotionOptions = listOf(QUEEN, ROOK, BISHOP, KNIGHT)
    }
}

@Immutable
data class ChessPiece(
    val id: Int,
    val type: PieceType,
    val color: PieceColor,
    val hasMoved: Boolean = false
)

@Immutable
data class BoardPos(
    val row: Int,
    val col: Int
) {
    fun isValid(): Boolean = row in 0..7 && col in 0..7

    fun toAlgebraic(): String {
        val file = ('a' + col)
        val rank = (8 - row)
        return "$file$rank"
    }

    val isLightSquare: Boolean
        get() = (row + col) % 2 == 0
}

@Immutable
data class ChessMove(
    val from: BoardPos,
    val to: BoardPos,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val capturedPos: BoardPos? = null,
    val isEnPassant: Boolean = false,
    val isCastlingKingside: Boolean = false,
    val isCastlingQueenside: Boolean = false,
    val isPromotion: Boolean = false,
    val promotionType: PieceType? = null,
    val algebraicNotation: String = ""
) {
    val isCapture: Boolean
        get() = capturedPiece != null
}

@Immutable
data class MovePair(
    val moveNumber: Int,
    val whiteMove: String,
    val blackMove: String? = null,
    val isWhiteCapture: Boolean = false,
    val isBlackCapture: Boolean = false,
    val isWhiteCheckOrMate: Boolean = false,
    val isBlackCheckOrMate: Boolean = false
)

enum class AIDifficulty(
    val label: String,
    val shortTitle: String,
    val searchDepth: Int,
    val description: String
) {
    NOVICE(
        label = "Novice (1-2 depth)",
        shortTitle = "Novice",
        searchDepth = 2,
        description = "Fast tactical evaluation looking 2 plies ahead"
    ),
    TACTICIAN(
        label = "Challenger (2-3 depth)",
        shortTitle = "Challenger",
        searchDepth = 3,
        description = "Balanced Minimax with positional awareness"
    ),
    MASTER(
        label = "Master (3-4 depth)",
        shortTitle = "Master",
        searchDepth = 4,
        description = "Alpha-Beta pruning + Piece-Square Tables (3-4 plies)"
    )
}

enum class GameStatusType {
    ACTIVE,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVE,
    DRAW_INSUFFICIENT_MATERIAL,
    RESIGNED,
    TIMEOUT
}

enum class KillImpactStyle(val title: String) {
    SHATTER_OBSIDIAN("Shatter Strike"),
    SOLAR_VORTEX("Solar Vortex"),
    THUNDER_CLEAVE("Thunder Cleave")
}

@Immutable
data class KillAnimationEvent(
    val eventId: Long = System.currentTimeMillis(),
    val attacker: ChessPiece,
    val victim: ChessPiece,
    val from: BoardPos,
    val to: BoardPos,
    val victimPos: BoardPos = to,
    val moveNotation: String,
    val impactStyle: KillImpactStyle = KillImpactStyle.SHATTER_OBSIDIAN,
    val isReplayOrDemo: Boolean = false
)

@Immutable
data class PendingPromotion(
    val from: BoardPos,
    val to: BoardPos,
    val pawn: ChessPiece,
    val capturedPiece: ChessPiece?,
    val selectedType: PieceType = PieceType.QUEEN
)
