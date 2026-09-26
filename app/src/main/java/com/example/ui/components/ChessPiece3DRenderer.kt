package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.PieceColor
import com.example.model.PieceType

@Composable
fun ChessPiece3DIcon(
    type: PieceType,
    color: PieceColor,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isInCheck: Boolean = false,
    elevationScale: Float = 1f
) {
    Canvas(modifier = modifier) {
        draw3DChessPiece(
            type = type,
            color = color,
            center = Offset(size.width / 2f, size.height / 2f),
            pieceSize = size.minDimension * 0.88f,
            isSelected = isSelected,
            isInCheck = isInCheck,
            elevationScale = elevationScale
        )
    }
}

fun DrawScope.draw3DChessPiece(
    type: PieceType,
    color: PieceColor,
    center: Offset,
    pieceSize: Float,
    isSelected: Boolean = false,
    isInCheck: Boolean = false,
    elevationScale: Float = 1f,
    alpha: Float = 1f,
    rotationDegrees: Float = 0f
) {
    val w = pieceSize
    val h = pieceSize

    withTransform({
        translate(center.x, center.y)
        if (rotationDegrees != 0f) {
            rotate(rotationDegrees, Offset.Zero)
        }
        if (elevationScale != 1f) {
            scale(elevationScale, elevationScale, Offset.Zero)
        }
    }) {
        val isWhite = color == PieceColor.WHITE

        // 1. Cast 3D Ground Shadow on the marble square
        val shadowOffsetY = h * 0.36f
        drawOval(
            color = Color.Black.copy(alpha = 0.58f * alpha),
            topLeft = Offset(-w * 0.40f, shadowOffsetY - h * 0.08f),
            size = Size(w * 0.80f, h * 0.22f)
        )

        // 2. Selection or Check Aura Halo under pedestal
        if (isSelected) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.75f * alpha),
                        Color(0xFFFBBF24).copy(alpha = 0.40f * alpha),
                        Color.Transparent
                    ),
                    center = Offset(0f, shadowOffsetY),
                    radius = w * 0.55f
                ),
                topLeft = Offset(-w * 0.52f, shadowOffsetY - h * 0.16f),
                size = Size(w * 1.04f, h * 0.34f)
            )
        } else if (isInCheck) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.85f * alpha),
                        Color(0xFFDC2626).copy(alpha = 0.35f * alpha),
                        Color.Transparent
                    ),
                    center = Offset(0f, shadowOffsetY),
                    radius = w * 0.56f
                ),
                topLeft = Offset(-w * 0.54f, shadowOffsetY - h * 0.16f),
                size = Size(w * 1.08f, h * 0.34f)
            )
        }

        // High-contrast 3D material brushes
        val bodyBrush = if (isWhite) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = alpha), // Bright top-left 3D specular
                    Color(0xFFF7F0E4).copy(alpha = alpha), // Warm Alabaster Ivory
                    Color(0xFFD9C7A7).copy(alpha = alpha), // Sculpted marble mid-shadow
                    Color(0xFFA8926E).copy(alpha = alpha)  // Deep bronze-ivory base shadow
                ),
                start = Offset(-w * 0.35f, -h * 0.45f),
                end = Offset(w * 0.38f, h * 0.45f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF64748B).copy(alpha = alpha), // Metallic slate top-left specular
                    Color(0xFF283548).copy(alpha = alpha), // Polished dark obsidian chrome
                    Color(0xFF111827).copy(alpha = alpha), // Deep midnight obsidian core
                    Color(0xFF060911).copy(alpha = alpha)  // Shadow edge
                ),
                start = Offset(-w * 0.35f, -h * 0.45f),
                end = Offset(w * 0.38f, h * 0.45f)
            )
        }

        // High-visibility outer rim & inner accent colors
        val outerContourColor = if (isWhite) {
            Color(0xFF1A140E).copy(alpha = 0.95f * alpha)
        } else {
            // Luminous silver-platinum edge so Black pieces pop sharply against dark tiles
            Color(0xFFE2E8F0).copy(alpha = 0.92f * alpha)
        }

        val secondaryRimColor = if (isWhite) {
            Color(0xFFFDE68A).copy(alpha = 0.90f * alpha)
        } else {
            Color(0xFF38BDF8).copy(alpha = 0.55f * alpha)
        }

        val trimColor = if (isWhite) {
            Color(0xFFD97706).copy(alpha = alpha) // Royal Gold trim
        } else {
            Color(0xFFEF4444).copy(alpha = alpha) // Glowing Ruby/Crimson trim on Black pieces
        }

        val highlightOrbColor = if (isWhite) {
            Color(0xFFFEF08A).copy(alpha = alpha)
        } else {
            Color(0xFFFCA5A5).copy(alpha = alpha)
        }

        // 3. Draw 3D Tiered Pedestal Base (common to all pieces for 3D figurine depth)
        draw3DPedestal(
            w = w,
            h = h,
            isWhite = isWhite,
            bodyBrush = bodyBrush,
            outerContourColor = outerContourColor,
            trimColor = trimColor,
            alpha = alpha
        )

        // 4. Draw Piece-Specific 3D Sculpted Body & Crown
        val bodyPath = buildPieceSilhouettePath(type, w, h)

        // Outer contrast halo for Black pieces so they never blend into dark squares
        if (!isWhite) {
            drawPath(
                path = bodyPath,
                color = Color(0xFF94A3B8).copy(alpha = 0.42f * alpha),
                style = Stroke(width = w * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            drawPath(
                path = bodyPath,
                color = Color.Black.copy(alpha = 0.45f * alpha),
                style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Fill 3D sculpted body
        drawPath(
            path = bodyPath,
            brush = bodyBrush
        )

        // Inner 3D Specular Rim Reflection along left side
        drawPath(
            path = bodyPath,
            color = secondaryRimColor,
            style = Stroke(width = w * 0.032f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Crisp Outer Definition Stroke
        drawPath(
            path = bodyPath,
            color = outerContourColor,
            style = Stroke(width = w * 0.024f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. Piece-Specific 3D Details, Crown Jewels, and Specular Highlights
        drawPieceInteriorDetails(
            type = type,
            w = w,
            h = h,
            isWhite = isWhite,
            trimColor = trimColor,
            highlightOrbColor = highlightOrbColor,
            outerContourColor = outerContourColor,
            alpha = alpha
        )
    }
}

private fun DrawScope.draw3DPedestal(
    w: Float,
    h: Float,
    isWhite: Boolean,
    bodyBrush: Brush,
    outerContourColor: Color,
    trimColor: Color,
    alpha: Float
) {
    val baseBottomY = h * 0.36f
    val baseTopY = h * 0.23f

    // Lower elliptical pedestal rim
    drawRoundRect(
        brush = bodyBrush,
        topLeft = Offset(-w * 0.34f, baseTopY),
        size = Size(w * 0.68f, baseBottomY - baseTopY),
        cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
    )
    drawRoundRect(
        color = outerContourColor,
        topLeft = Offset(-w * 0.34f, baseTopY),
        size = Size(w * 0.68f, baseBottomY - baseTopY),
        cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
        style = Stroke(width = w * 0.025f)
    )

    // Glowing metallic collar ring on pedestal
    drawLine(
        color = trimColor,
        start = Offset(-w * 0.28f, baseTopY + (baseBottomY - baseTopY) * 0.45f),
        end = Offset(w * 0.28f, baseTopY + (baseBottomY - baseTopY) * 0.45f),
        strokeWidth = w * 0.035f,
        cap = StrokeCap.Round
    )

    // Upper pedestal step
    drawRoundRect(
        brush = bodyBrush,
        topLeft = Offset(-w * 0.27f, h * 0.16f),
        size = Size(w * 0.54f, h * 0.09f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = outerContourColor,
        topLeft = Offset(-w * 0.27f, h * 0.16f),
        size = Size(w * 0.54f, h * 0.09f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        style = Stroke(width = w * 0.02f)
    )
}

private fun buildPieceSilhouettePath(type: PieceType, w: Float, h: Float): Path {
    return Path().apply {
        when (type) {
            PieceType.PAWN -> {
                // Sculpted stem from pedestal up to collar
                moveTo(-w * 0.22f, h * 0.17f)
                cubicTo(-w * 0.16f, h * 0.04f, -w * 0.11f, -h * 0.06f, -w * 0.15f, -h * 0.12f)
                lineTo(w * 0.15f, -h * 0.12f)
                cubicTo(w * 0.11f, -h * 0.06f, w * 0.16f, h * 0.04f, w * 0.22f, h * 0.17f)
                close()

                // Spherical 3D head
                addOval(
                    Rect(
                        left = -w * 0.17f,
                        top = -h * 0.38f,
                        right = w * 0.17f,
                        bottom = -h * 0.06f
                    )
                )
            }

            PieceType.ROOK -> {
                // Strong tapered castle tower with 3 battlements
                moveTo(-w * 0.23f, h * 0.17f)
                lineTo(-w * 0.18f, -h * 0.16f)
                lineTo(-w * 0.24f, -h * 0.21f)
                lineTo(-w * 0.24f, -h * 0.36f)
                lineTo(-w * 0.13f, -h * 0.36f)
                lineTo(-w * 0.13f, -h * 0.28f)
                lineTo(-w * 0.06f, -h * 0.28f)
                lineTo(-w * 0.06f, -h * 0.36f)
                lineTo(w * 0.06f, -h * 0.36f)
                lineTo(w * 0.06f, -h * 0.28f)
                lineTo(w * 0.13f, -h * 0.28f)
                lineTo(w * 0.13f, -h * 0.36f)
                lineTo(w * 0.24f, -h * 0.36f)
                lineTo(w * 0.24f, -h * 0.21f)
                lineTo(w * 0.18f, -h * 0.16f)
                lineTo(w * 0.23f, h * 0.17f)
                close()
            }

            PieceType.KNIGHT -> {
                // Dynamic warhorse bust with arched neck, ears, and snout
                moveTo(-w * 0.23f, h * 0.17f)
                cubicTo(-w * 0.24f, -h * 0.02f, -w * 0.26f, -h * 0.22f, -w * 0.10f, -h * 0.35f)
                // Pointed ear
                lineTo(-w * 0.06f, -h * 0.43f)
                lineTo(w * 0.02f, -h * 0.34f)
                // Forehead & snout
                cubicTo(w * 0.16f, -h * 0.30f, w * 0.26f, -h * 0.18f, w * 0.24f, -h * 0.10f)
                // Mouth / jaw indentation
                lineTo(w * 0.11f, -h * 0.06f)
                cubicTo(w * 0.05f, -h * 0.07f, w * 0.04f, -h * 0.01f, w * 0.14f, h * 0.04f)
                cubicTo(w * 0.20f, h * 0.08f, w * 0.22f, h * 0.12f, w * 0.23f, h * 0.17f)
                close()
            }

            PieceType.BISHOP -> {
                // Tall sculpted mitre + tapered column
                moveTo(-w * 0.22f, h * 0.17f)
                cubicTo(-w * 0.16f, h * 0.03f, -w * 0.13f, -h * 0.06f, -w * 0.18f, -h * 0.14f)
                cubicTo(-w * 0.24f, -h * 0.24f, -w * 0.14f, -h * 0.37f, 0f, -h * 0.41f)
                cubicTo(w * 0.14f, -h * 0.37f, w * 0.24f, -h * 0.24f, w * 0.18f, -h * 0.14f)
                cubicTo(w * 0.13f, -h * 0.06f, w * 0.16f, h * 0.03f, w * 0.22f, h * 0.17f)
                close()
            }

            PieceType.QUEEN -> {
                // Regal flared cornet with 5 crown spikes
                moveTo(-w * 0.24f, h * 0.17f)
                cubicTo(-w * 0.18f, h * 0.01f, -w * 0.18f, -h * 0.10f, -w * 0.28f, -h * 0.31f)
                lineTo(-w * 0.15f, -h * 0.19f)
                lineTo(-w * 0.13f, -h * 0.36f)
                lineTo(-w * 0.04f, -h * 0.21f)
                lineTo(0f, -h * 0.39f)
                lineTo(w * 0.04f, -h * 0.21f)
                lineTo(w * 0.13f, -h * 0.36f)
                lineTo(w * 0.15f, -h * 0.19f)
                lineTo(w * 0.28f, -h * 0.31f)
                cubicTo(w * 0.18f, -h * 0.10f, w * 0.18f, h * 0.01f, w * 0.24f, h * 0.17f)
                close()
            }

            PieceType.KING -> {
                // Imperial arched crown + tall column
                moveTo(-w * 0.24f, h * 0.17f)
                cubicTo(-w * 0.18f, h * 0.02f, -w * 0.16f, -h * 0.10f, -w * 0.24f, -h * 0.25f)
                cubicTo(-w * 0.16f, -h * 0.34f, -w * 0.06f, -h * 0.35f, 0f, -h * 0.31f)
                cubicTo(w * 0.06f, -h * 0.35f, w * 0.16f, -h * 0.34f, w * 0.24f, -h * 0.25f)
                cubicTo(w * 0.16f, -h * 0.10f, w * 0.18f, h * 0.02f, w * 0.24f, h * 0.17f)
                close()
            }
        }
    }
}

private fun DrawScope.drawPieceInteriorDetails(
    type: PieceType,
    w: Float,
    h: Float,
    isWhite: Boolean,
    trimColor: Color,
    highlightOrbColor: Color,
    outerContourColor: Color,
    alpha: Float
) {
    // 3D Vertical Specular Shine Strip on the left side of the body
    drawLine(
        color = Color.White.copy(alpha = (if (isWhite) 0.70f else 0.38f) * alpha),
        start = Offset(-w * 0.09f, -h * 0.12f),
        end = Offset(-w * 0.12f, h * 0.12f),
        strokeWidth = w * 0.045f,
        cap = StrokeCap.Round
    )

    // Golden / Crimson Neck Collar Ring
    if (type != PieceType.KNIGHT) {
        drawLine(
            color = trimColor,
            start = Offset(-w * 0.16f, -h * 0.11f),
            end = Offset(w * 0.16f, -h * 0.11f),
            strokeWidth = w * 0.038f,
            cap = StrokeCap.Round
        )
    }

    when (type) {
        PieceType.PAWN -> {
            // Specular glint on the pawn sphere
            drawCircle(
                color = Color.White.copy(alpha = (if (isWhite) 0.85f else 0.50f) * alpha),
                radius = w * 0.045f,
                center = Offset(-w * 0.06f, -h * 0.27f)
            )
        }

        PieceType.ROOK -> {
            // Fortified horizontal masonry lines
            drawLine(
                color = trimColor,
                start = Offset(-w * 0.18f, -h * 0.19f),
                end = Offset(w * 0.18f, -h * 0.19f),
                strokeWidth = w * 0.032f,
                cap = StrokeCap.Round
            )
        }

        PieceType.KNIGHT -> {
            // Glowing eye & golden/crimson mane ridge
            drawCircle(
                color = highlightOrbColor,
                radius = w * 0.032f,
                center = Offset(w * 0.05f, -h * 0.24f)
            )
            // Mane accent line
            val manePath = Path().apply {
                moveTo(-w * 0.08f, -h * 0.33f)
                cubicTo(-w * 0.19f, -h * 0.18f, -w * 0.18f, -h * 0.02f, -w * 0.15f, h * 0.12f)
            }
            drawPath(
                path = manePath,
                color = trimColor,
                style = Stroke(width = w * 0.038f, cap = StrokeCap.Round)
            )
        }

        PieceType.BISHOP -> {
            // Top finial orb
            drawCircle(
                color = trimColor,
                radius = w * 0.048f,
                center = Offset(0f, -h * 0.44f)
            )
            drawCircle(
                color = outerContourColor,
                radius = w * 0.048f,
                center = Offset(0f, -h * 0.44f),
                style = Stroke(width = w * 0.018f)
            )
            // Mitre sacred cross/slit
            drawLine(
                color = highlightOrbColor,
                start = Offset(0f, -h * 0.32f),
                end = Offset(0f, -h * 0.20f),
                strokeWidth = w * 0.032f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = highlightOrbColor,
                start = Offset(-w * 0.05f, -h * 0.26f),
                end = Offset(w * 0.05f, -h * 0.26f),
                strokeWidth = w * 0.032f,
                cap = StrokeCap.Round
            )
        }

        PieceType.QUEEN -> {
            // 5 Crown Jewels atop the Queen's diadem points
            val points = listOf(
                Offset(-w * 0.28f, -h * 0.33f),
                Offset(-w * 0.13f, -h * 0.38f),
                Offset(0f, -h * 0.41f),
                Offset(w * 0.13f, -h * 0.38f),
                Offset(w * 0.28f, -h * 0.33f)
            )
            for (pt in points) {
                drawCircle(
                    color = highlightOrbColor,
                    radius = w * 0.034f,
                    center = pt
                )
                drawCircle(
                    color = outerContourColor,
                    radius = w * 0.034f,
                    center = pt,
                    style = Stroke(width = w * 0.014f)
                )
            }
        }

        PieceType.KING -> {
            // Royal Cross atop the King (glowing gold for White, glowing crimson/ruby for Black like the reference art!)
            val crossCenterY = -h * 0.41f
            // Outer glow behind the King's cross
            drawCircle(
                color = trimColor.copy(alpha = 0.45f * alpha),
                radius = w * 0.12f,
                center = Offset(0f, crossCenterY)
            )
            // Vertical bar of cross
            drawLine(
                color = trimColor,
                start = Offset(0f, -h * 0.49f),
                end = Offset(0f, -h * 0.31f),
                strokeWidth = w * 0.058f,
                cap = StrokeCap.Round
            )
            // Horizontal bar of cross
            drawLine(
                color = trimColor,
                start = Offset(-w * 0.085f, crossCenterY),
                end = Offset(w * 0.085f, crossCenterY),
                strokeWidth = w * 0.058f,
                cap = StrokeCap.Round
            )
            // Bright center jewel on cross
            drawCircle(
                color = highlightOrbColor,
                radius = w * 0.028f,
                center = Offset(0f, crossCenterY)
            )
        }
    }
}
