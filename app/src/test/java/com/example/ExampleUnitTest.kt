package com.example

import com.example.engine.ChessAI
import com.example.engine.ChessEngine
import com.example.model.AIDifficulty
import com.example.model.BoardPos
import com.example.model.PieceColor
import com.example.model.PieceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun initialBoard_hasTwentyLegalMovesForWhite() {
        val state = ChessEngine.createInitialState()
        val legalMoves = ChessEngine.getAllLegalMoves(state, PieceColor.WHITE)
        assertEquals(20, legalMoves.size)
        assertEquals(PieceColor.WHITE, state.turn)
    }

    @Test
    fun tacticalArenaScenario_generatesImmediateCaptureOpportunities() {
        val state = ChessEngine.createTacticalArenaScenario()
        assertEquals(PieceColor.WHITE, state.turn)
        val c3Moves = ChessEngine.getLegalMovesForSquare(state, BoardPos(5, 2))
        val captureD4 = c3Moves.firstOrNull { it.to == BoardPos(4, 3) && it.isCapture }
        assertNotNull(captureD4)
        assertEquals(PieceType.PAWN, captureD4?.capturedPiece?.type)
    }

    @Test
    fun minimaxAi_findsValidMoveAtNoviceAndMasterDepths() {
        val state = ChessEngine.createTacticalArenaScenario()
        val noviceMove = ChessAI.findBestMove(state, AIDifficulty.NOVICE, PieceColor.WHITE)
        val masterMove = ChessAI.findBestMove(state, AIDifficulty.MASTER, PieceColor.WHITE)
        assertNotNull(noviceMove)
        assertNotNull(masterMove)
        assertTrue(ChessEngine.getAllLegalMoves(state, PieceColor.WHITE).any { it.from == masterMove?.from && it.to == masterMove.to })
    }
}
