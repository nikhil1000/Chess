package com.example.engine

import com.example.model.BoardPos
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.GameStatusType
import com.example.model.PieceColor
import com.example.model.PieceType
import kotlin.math.abs

data class BoardState(
    val grid: List<List<ChessPiece?>>, // 8x8 matrix
    val turn: PieceColor = PieceColor.WHITE,
    val enPassantTarget: BoardPos? = null,
    val capturedByWhite: List<ChessPiece> = emptyList(), // Black pieces captured by White
    val capturedByBlack: List<ChessPiece> = emptyList(), // White pieces captured by Black
    val moveHistory: List<ChessMove> = emptyList(),
    val lastMove: ChessMove? = null,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
    val status: GameStatusType = GameStatusType.ACTIVE,
    val kingInCheckPos: BoardPos? = null,
    val winner: PieceColor? = null
) {
    fun pieceAt(pos: BoardPos): ChessPiece? {
        if (!pos.isValid()) return null
        return grid[pos.row][pos.col]
    }

    fun pieceAt(row: Int, col: Int): ChessPiece? {
        if (row !in 0..7 || col !in 0..7) return null
        return grid[row][col]
    }
}

object ChessEngine {

    fun createInitialState(): BoardState {
        var nextId = 1
        val grid = MutableList(8) { MutableList<ChessPiece?>(8) { null } }

        val backRankOrder = listOf(
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        )

        // Black pieces (rows 0 and 1)
        for (col in 0..7) {
            grid[0][col] = ChessPiece(id = nextId++, type = backRankOrder[col], color = PieceColor.BLACK)
            grid[1][col] = ChessPiece(id = nextId++, type = PieceType.PAWN, color = PieceColor.BLACK)
        }

        // White pieces (rows 6 and 7)
        for (col in 0..7) {
            grid[6][col] = ChessPiece(id = nextId++, type = PieceType.PAWN, color = PieceColor.WHITE)
            grid[7][col] = ChessPiece(id = nextId++, type = backRankOrder[col], color = PieceColor.WHITE)
        }

        return BoardState(
            grid = grid.map { it.toList() },
            turn = PieceColor.WHITE
        )
    }

    /**
     * Plays out the classic Ruy Lopez Breyer tactical opening up to move 11...exd4
     * so the user can also jump straight into an action-packed midgame with immediate captures!
     */
    fun createTacticalArenaScenario(): BoardState {
        var state = createInitialState()
        val scriptMoves = listOf(
            BoardPos(6, 4) to BoardPos(4, 4), // 1. e4
            BoardPos(1, 4) to BoardPos(3, 4), // 1... e5
            BoardPos(7, 6) to BoardPos(5, 5), // 2. Nf3
            BoardPos(0, 1) to BoardPos(2, 2), // 2... Nc6
            BoardPos(7, 5) to BoardPos(3, 1), // 3. Bb5
            BoardPos(1, 0) to BoardPos(2, 0), // 3... a6
            BoardPos(3, 1) to BoardPos(4, 0), // 4. Ba4
            BoardPos(0, 6) to BoardPos(2, 5), // 4... Nf6
            BoardPos(7, 4) to BoardPos(7, 6), // 5. O-O
            BoardPos(0, 5) to BoardPos(1, 4), // 5... Be7
            BoardPos(7, 5) to BoardPos(7, 4), // 6. Re1
            BoardPos(1, 1) to BoardPos(3, 1), // 6... b5
            BoardPos(4, 0) to BoardPos(5, 1), // 7. Bb3
            BoardPos(1, 3) to BoardPos(2, 3), // 7... d6
            BoardPos(6, 2) to BoardPos(5, 2), // 8. c3
            BoardPos(0, 4) to BoardPos(0, 6), // 8... O-O
            BoardPos(6, 7) to BoardPos(5, 7), // 9. h3
            BoardPos(2, 2) to BoardPos(0, 1), // 9... Nb8
            BoardPos(6, 3) to BoardPos(4, 3), // 10. d4
            BoardPos(0, 1) to BoardPos(1, 3), // 10... Nbd7
            BoardPos(7, 2) to BoardPos(3, 6), // 11. Bg5
            BoardPos(3, 4) to BoardPos(4, 3)  // 11... exd4 (Black captures on d4, ready for White 12. cxd4 or Nxd4!)
        )

        for ((from, to) in scriptMoves) {
            val legalMoves = getLegalMovesForSquare(state, from)
            val chosen = legalMoves.firstOrNull { it.to == to }
            if (chosen != null) {
                state = applyMove(state, chosen)
            }
        }
        return state
    }

    fun getLegalMovesForSquare(state: BoardState, from: BoardPos): List<ChessMove> {
        val piece = state.pieceAt(from) ?: return emptyList()
        if (piece.color != state.turn) return emptyList()
        val pseudoMoves = generatePseudoLegalMovesForPiece(state, from, piece, includeCastling = true)
        return pseudoMoves.filter { move ->
            val nextGrid = applyMoveToGridOnly(state.grid, move)
            !isKingInCheck(nextGrid, piece.color)
        }
    }

    fun getAllLegalMoves(state: BoardState, color: PieceColor = state.turn): List<ChessMove> {
        val result = mutableListOf<ChessMove>()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = state.grid[r][c]
                if (p != null && p.color == color) {
                    val pos = BoardPos(r, c)
                    val pseudo = generatePseudoLegalMovesForPiece(state, pos, p, includeCastling = true)
                    for (move in pseudo) {
                        val nextGrid = applyMoveToGridOnly(state.grid, move)
                        if (!isKingInCheck(nextGrid, color)) {
                            result.add(move)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun generatePseudoLegalMovesForPiece(
        state: BoardState,
        from: BoardPos,
        piece: ChessPiece,
        includeCastling: Boolean
    ): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()
        val r = from.row
        val c = from.col

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
                val promoRow = if (piece.color == PieceColor.WHITE) 0 else 7

                // 1 step forward
                val oneStep = BoardPos(r + dir, c)
                if (oneStep.isValid() && state.pieceAt(oneStep) == null) {
                    val isPromo = oneStep.row == promoRow
                    moves.add(
                        ChessMove(
                            from = from,
                            to = oneStep,
                            piece = piece,
                            isPromotion = isPromo,
                            promotionType = if (isPromo) PieceType.QUEEN else null
                        )
                    )
                    // 2 steps forward from starting rank
                    val twoStep = BoardPos(r + dir * 2, c)
                    if (r == startRow && twoStep.isValid() && state.pieceAt(twoStep) == null) {
                        moves.add(
                            ChessMove(
                                from = from,
                                to = twoStep,
                                piece = piece
                            )
                        )
                    }
                }

                // Diagonal captures & En Passant
                for (dc in listOf(-1, 1)) {
                    val diag = BoardPos(r + dir, c + dc)
                    if (!diag.isValid()) continue
                    val target = state.pieceAt(diag)
                    if (target != null && target.color != piece.color) {
                        val isPromo = diag.row == promoRow
                        moves.add(
                            ChessMove(
                                from = from,
                                to = diag,
                                piece = piece,
                                capturedPiece = target,
                                capturedPos = diag,
                                isPromotion = isPromo,
                                promotionType = if (isPromo) PieceType.QUEEN else null
                            )
                        )
                    } else if (state.enPassantTarget == diag) {
                        val epPawnPos = BoardPos(r, c + dc)
                        val epVictim = state.pieceAt(epPawnPos)
                        if (epVictim != null && epVictim.color != piece.color && epVictim.type == PieceType.PAWN) {
                            moves.add(
                                ChessMove(
                                    from = from,
                                    to = diag,
                                    piece = piece,
                                    capturedPiece = epVictim,
                                    capturedPos = epPawnPos,
                                    isEnPassant = true
                                )
                            )
                        }
                    }
                }
            }

            PieceType.KNIGHT -> {
                val offsets = listOf(
                    -2 to -1, -2 to 1,
                    -1 to -2, -1 to 2,
                    1 to -2, 1 to 2,
                    2 to -1, 2 to 1
                )
                for ((dr, dc) in offsets) {
                    val to = BoardPos(r + dr, c + dc)
                    if (!to.isValid()) continue
                    val target = state.pieceAt(to)
                    if (target == null || target.color != piece.color) {
                        moves.add(
                            ChessMove(
                                from = from,
                                to = to,
                                piece = piece,
                                capturedPiece = target,
                                capturedPos = if (target != null) to else null
                            )
                        )
                    }
                }
            }

            PieceType.BISHOP -> {
                val dirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
                addSlidingMoves(state, from, piece, dirs, moves)
            }

            PieceType.ROOK -> {
                val dirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                addSlidingMoves(state, from, piece, dirs, moves)
            }

            PieceType.QUEEN -> {
                val dirs = listOf(
                    -1 to -1, -1 to 1, 1 to -1, 1 to 1,
                    -1 to 0, 1 to 0, 0 to -1, 0 to 1
                )
                addSlidingMoves(state, from, piece, dirs, moves)
            }

            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val to = BoardPos(r + dr, c + dc)
                        if (!to.isValid()) continue
                        val target = state.pieceAt(to)
                        if (target == null || target.color != piece.color) {
                            moves.add(
                                ChessMove(
                                    from = from,
                                    to = to,
                                    piece = piece,
                                    capturedPiece = target,
                                    capturedPos = if (target != null) to else null
                                )
                            )
                        }
                    }
                }

                // Castling
                if (includeCastling && !piece.hasMoved && !isKingInCheck(state.grid, piece.color)) {
                    val homeRow = if (piece.color == PieceColor.WHITE) 7 else 0
                    if (r == homeRow && c == 4) {
                        val enemyColor = piece.color.opposite()
                        // Kingside (O-O)
                        val ksRook = state.pieceAt(homeRow, 7)
                        if (ksRook != null && ksRook.type == PieceType.ROOK && !ksRook.hasMoved) {
                            if (state.pieceAt(homeRow, 5) == null && state.pieceAt(homeRow, 6) == null) {
                                val fSquareAttacked = isSquareAttacked(state.grid, BoardPos(homeRow, 5), enemyColor)
                                val gSquareAttacked = isSquareAttacked(state.grid, BoardPos(homeRow, 6), enemyColor)
                                if (!fSquareAttacked && !gSquareAttacked) {
                                    moves.add(
                                        ChessMove(
                                            from = from,
                                            to = BoardPos(homeRow, 6),
                                            piece = piece,
                                            isCastlingKingside = true
                                        )
                                    )
                                }
                            }
                        }

                        // Queenside (O-O-O)
                        val qsRook = state.pieceAt(homeRow, 0)
                        if (qsRook != null && qsRook.type == PieceType.ROOK && !qsRook.hasMoved) {
                            if (
                                state.pieceAt(homeRow, 1) == null &&
                                state.pieceAt(homeRow, 2) == null &&
                                state.pieceAt(homeRow, 3) == null
                            ) {
                                val dSquareAttacked = isSquareAttacked(state.grid, BoardPos(homeRow, 3), enemyColor)
                                val cSquareAttacked = isSquareAttacked(state.grid, BoardPos(homeRow, 2), enemyColor)
                                if (!dSquareAttacked && !cSquareAttacked) {
                                    moves.add(
                                        ChessMove(
                                            from = from,
                                            to = BoardPos(homeRow, 2),
                                            piece = piece,
                                            isCastlingQueenside = true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return moves
    }

    private fun addSlidingMoves(
        state: BoardState,
        from: BoardPos,
        piece: ChessPiece,
        dirs: List<Pair<Int, Int>>,
        outMoves: MutableList<ChessMove>
    ) {
        for ((dr, dc) in dirs) {
            var nr = from.row + dr
            var nc = from.col + dc
            while (nr in 0..7 && nc in 0..7) {
                val to = BoardPos(nr, nc)
                val target = state.grid[nr][nc]
                if (target == null) {
                    outMoves.add(
                        ChessMove(
                            from = from,
                            to = to,
                            piece = piece
                        )
                    )
                } else {
                    if (target.color != piece.color) {
                        outMoves.add(
                            ChessMove(
                                from = from,
                                to = to,
                                piece = piece,
                                capturedPiece = target,
                                capturedPos = to
                            )
                        )
                    }
                    break
                }
                nr += dr
                nc += dc
            }
        }
    }

    fun applyMoveToGridOnly(
        grid: List<List<ChessPiece?>>,
        move: ChessMove
    ): List<List<ChessPiece?>> {
        val mutable = grid.map { it.toMutableList() }.toMutableList()
        mutable[move.from.row][move.from.col] = null

        if (move.isEnPassant && move.capturedPos != null) {
            mutable[move.capturedPos.row][move.capturedPos.col] = null
        }

        val finalType = if (move.isPromotion && move.promotionType != null) {
            move.promotionType
        } else {
            move.piece.type
        }

        mutable[move.to.row][move.to.col] = move.piece.copy(
            type = finalType,
            hasMoved = true
        )

        if (move.isCastlingKingside) {
            val row = move.from.row
            val rook = mutable[row][7]
            mutable[row][7] = null
            if (rook != null) {
                mutable[row][5] = rook.copy(hasMoved = true)
            }
        } else if (move.isCastlingQueenside) {
            val row = move.from.row
            val rook = mutable[row][0]
            mutable[row][0] = null
            if (rook != null) {
                mutable[row][3] = rook.copy(hasMoved = true)
            }
        }

        return mutable
    }

    fun applyMove(
        state: BoardState,
        rawMove: ChessMove
    ): BoardState {
        val nextGrid = applyMoveToGridOnly(state.grid, rawMove)
        val nextTurn = state.turn.opposite()

        // Determine if opponent is in check / checkmate / stalemate
        val inCheck = isKingInCheck(nextGrid, nextTurn)
        val checkKingPos = if (inCheck) findKingPosition(nextGrid, nextTurn) else null

        val newEnPassant = if (
            rawMove.piece.type == PieceType.PAWN &&
            abs(rawMove.to.row - rawMove.from.row) == 2
        ) {
            BoardPos((rawMove.from.row + rawMove.to.row) / 2, rawMove.from.col)
        } else {
            null
        }

        val tempNextState = state.copy(
            grid = nextGrid,
            turn = nextTurn,
            enPassantTarget = newEnPassant
        )

        val opponentLegalMoves = getAllLegalMoves(tempNextState, nextTurn)
        val isMate = inCheck && opponentLegalMoves.isEmpty()
        val isStalemate = !inCheck && opponentLegalMoves.isEmpty()

        val notation = formatAlgebraicNotation(
            beforeState = state,
            move = rawMove,
            givesCheck = inCheck,
            givesCheckmate = isMate
        )
        val finalizedMove = rawMove.copy(algebraicNotation = notation)

        val updatedCapturedByWhite = if (rawMove.capturedPiece != null && rawMove.piece.color == PieceColor.WHITE) {
            state.capturedByWhite + rawMove.capturedPiece
        } else {
            state.capturedByWhite
        }

        val updatedCapturedByBlack = if (rawMove.capturedPiece != null && rawMove.piece.color == PieceColor.BLACK) {
            state.capturedByBlack + rawMove.capturedPiece
        } else {
            state.capturedByBlack
        }

        val nextHalfMove = if (rawMove.piece.type == PieceType.PAWN || rawMove.isCapture) {
            0
        } else {
            state.halfMoveClock + 1
        }

        val nextFullMove = if (state.turn == PieceColor.BLACK) {
            state.fullMoveNumber + 1
        } else {
            state.fullMoveNumber
        }

        val nextStatus = when {
            isMate -> GameStatusType.CHECKMATE
            isStalemate -> GameStatusType.STALEMATE
            isInsufficientMaterial(nextGrid) -> GameStatusType.DRAW_INSUFFICIENT_MATERIAL
            nextHalfMove >= 100 -> GameStatusType.DRAW_FIFTY_MOVE
            inCheck -> GameStatusType.CHECK
            else -> GameStatusType.ACTIVE
        }

        val winner = if (isMate) state.turn else null

        return state.copy(
            grid = nextGrid,
            turn = nextTurn,
            enPassantTarget = newEnPassant,
            capturedByWhite = updatedCapturedByWhite,
            capturedByBlack = updatedCapturedByBlack,
            moveHistory = state.moveHistory + finalizedMove,
            lastMove = finalizedMove,
            halfMoveClock = nextHalfMove,
            fullMoveNumber = nextFullMove,
            status = nextStatus,
            kingInCheckPos = checkKingPos,
            winner = winner
        )
    }

    fun findKingPosition(grid: List<List<ChessPiece?>>, color: PieceColor): BoardPos? {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = grid[r][c]
                if (p != null && p.color == color && p.type == PieceType.KING) {
                    return BoardPos(r, c)
                }
            }
        }
        return null
    }

    fun isKingInCheck(grid: List<List<ChessPiece?>>, kingColor: PieceColor): Boolean {
        val kingPos = findKingPosition(grid, kingColor) ?: return false
        return isSquareAttacked(grid, kingPos, kingColor.opposite())
    }

    fun isSquareAttacked(
        grid: List<List<ChessPiece?>>,
        targetPos: BoardPos,
        byColor: PieceColor
    ): Boolean {
        val r = targetPos.row
        val c = targetPos.col

        // 1. Attacked by pawns
        val pawnSourceRow = if (byColor == PieceColor.WHITE) r + 1 else r - 1
        if (pawnSourceRow in 0..7) {
            for (dc in listOf(-1, 1)) {
                val pc = c + dc
                if (pc in 0..7) {
                    val p = grid[pawnSourceRow][pc]
                    if (p != null && p.color == byColor && p.type == PieceType.PAWN) {
                        return true
                    }
                }
            }
        }

        // 2. Attacked by knights
        val knightOffsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in knightOffsets) {
            val nr = r + dr
            val nc = c + dc
            if (nr in 0..7 && nc in 0..7) {
                val p = grid[nr][nc]
                if (p != null && p.color == byColor && p.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // 3. Attacked by King
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0..7 && nc in 0..7) {
                    val p = grid[nr][nc]
                    if (p != null && p.color == byColor && p.type == PieceType.KING) {
                        return true
                    }
                }
            }
        }

        // 4. Orthogonal rays (Rook / Queen)
        val orthoDirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((dr, dc) in orthoDirs) {
            var nr = r + dr
            var nc = c + dc
            while (nr in 0..7 && nc in 0..7) {
                val p = grid[nr][nc]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                nr += dr
                nc += dc
            }
        }

        // 5. Diagonal rays (Bishop / Queen)
        val diagDirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        for ((dr, dc) in diagDirs) {
            var nr = r + dr
            var nc = c + dc
            while (nr in 0..7 && nc in 0..7) {
                val p = grid[nr][nc]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                nr += dr
                nc += dc
            }
        }

        return false
    }

    private fun isInsufficientMaterial(grid: List<List<ChessPiece?>>): Boolean {
        val pieces = mutableListOf<ChessPiece>()
        for (r in 0..7) {
            for (c in 0..7) {
                grid[r][c]?.let { pieces.add(it) }
            }
        }
        if (pieces.size == 2) return true // K vs K
        if (pieces.size == 3) {
            val minor = pieces.firstOrNull { it.type == PieceType.KNIGHT || it.type == PieceType.BISHOP }
            if (minor != null) return true
        }
        return false
    }

    private fun formatAlgebraicNotation(
        beforeState: BoardState,
        move: ChessMove,
        givesCheck: Boolean,
        givesCheckmate: Boolean
    ): String {
        val suffix = when {
            givesCheckmate -> "#"
            givesCheck -> "+"
            else -> ""
        }

        if (move.isCastlingKingside) return "O-O$suffix"
        if (move.isCastlingQueenside) return "O-O-O$suffix"

        val sb = StringBuilder()
        if (move.piece.type == PieceType.PAWN) {
            if (move.isCapture) {
                val fromFile = ('a' + move.from.col)
                sb.append(fromFile).append('x')
            }
            sb.append(move.to.toAlgebraic())
            if (move.isPromotion && move.promotionType != null) {
                sb.append('=').append(move.promotionType.algebraicSymbol)
            }
        } else {
            sb.append(move.piece.type.algebraicSymbol)
            // Disambiguation if another identical piece of the same color can also reach move.to
            val otherSamePiecesMoves = getAllLegalMoves(beforeState, move.piece.color).filter {
                it.piece.id != move.piece.id &&
                    it.piece.type == move.piece.type &&
                    it.to == move.to
            }
            if (otherSamePiecesMoves.isNotEmpty()) {
                val sameFile = otherSamePiecesMoves.any { it.from.col == move.from.col }
                val sameRank = otherSamePiecesMoves.any { it.from.row == move.from.row }
                when {
                    !sameFile -> sb.append('a' + move.from.col)
                    !sameRank -> sb.append(8 - move.from.row)
                    else -> sb.append(move.from.toAlgebraic())
                }
            }
            if (move.isCapture) {
                sb.append('x')
            }
            sb.append(move.to.toAlgebraic())
        }

        sb.append(suffix)
        return sb.toString()
    }
}
