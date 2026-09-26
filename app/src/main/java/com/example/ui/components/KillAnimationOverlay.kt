package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.BoardPos
import com.example.model.KillAnimationEvent
import com.example.model.PieceColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ShatterShard(
    val angleRad: Float,
    val speedFactor: Float,
    val spinSpeed: Float,
    val sizeFactor: Float,
    val isGlowingEmber: Boolean,
    val polygonPoints: List<Offset>,
    val gravityFactor: Float
)

object KillAnimationEngine {

    private var cachedEventId: Long = -1L
    private var cachedShards: List<ShatterShard> = emptyList()

    private fun getShardsForEvent(eventId: Long): List<ShatterShard> {
        if (cachedEventId == eventId && cachedShards.isNotEmpty()) {
            return cachedShards
        }
        val rng = Random(eventId)
        val generated = List(54) { idx ->
            val angle = (idx.toFloat() / 54f) * (2f * PI.toFloat()) + rng.nextFloat() * 0.26f
            val speed = 0.55f + rng.nextFloat() * 1.70f
            val spin = (rng.nextFloat() - 0.5f) * 960f
            val sz = 0.09f + rng.nextFloat() * 0.17f
            val isEmber = idx % 2 == 0
            val poly = listOf(
                Offset(-0.5f + rng.nextFloat() * 0.2f, -0.42f),
                Offset(0.52f, -0.3f + rng.nextFloat() * 0.3f),
                Offset(0.38f, 0.52f),
                Offset(-0.46f, 0.36f)
            )
            ShatterShard(
                angleRad = angle,
                speedFactor = speed,
                spinSpeed = spin,
                sizeFactor = sz,
                isGlowingEmber = isEmber,
                polygonPoints = poly,
                gravityFactor = 0.32f + rng.nextFloat() * 0.78f
            )
        }
        cachedEventId = eventId
        cachedShards = generated
        return generated
    }

    /**
     * Computes 3D board screen shake (translationX, translationY, rotationZ) during the impact phase.
     */
    fun computeBoardShake(progress: Float, maxShakePx: Float): Triple<Float, Float, Float> {
        if (progress < 0.30f || progress > 0.84f) return Triple(0f, 0f, 0f)
        val t = (progress - 0.30f) / 0.54f
        val decay = (1f - t) * (1f - t)
        val amp = maxShakePx * decay
        val dx = sin(t * 14f * PI.toFloat()) * amp
        val dy = cos(t * 11f * PI.toFloat()) * amp * 0.85f
        val rotZ = sin(t * 10f * PI.toFloat()) * 2.3f * decay
        return Triple(dx, dy, rotZ)
    }

    /**
     * Renders the glorified 3D combat capture sequence directly on the board Canvas:
     * 1. Attacker violently dashes along a blazing golden energy trail into the target square.
     * 2. Blinding impact flash + dual expanding shockwave rings.
     * 3. Captured piece violently spins & flies off the layout while shattering into 54 3D obsidian/marble shards & glowing fiery sparks.
     */
    fun DrawScope.drawKillAnimationSequence(
        event: KillAnimationEvent,
        progress: Float,
        tileSize: Float
    ) {
        val shards = getShardsForEvent(event.eventId)

        fun squareCenter(pos: BoardPos): Offset {
            return Offset((pos.col + 0.5f) * tileSize, (pos.row + 0.5f) * tileSize)
        }

        val startCenter = squareCenter(event.from)
        val endCenter = squareCenter(event.to)
        val victimCenter = squareCenter(event.victimPos)

        val impactMoment = 0.32f
        val dashProgress = (progress / impactMoment).coerceIn(0f, 1f)
        // High-impact ease-in-cubic acceleration
        val easedDash = dashProgress * dashProgress * (2.2f - 1.2f * dashProgress)

        // 1. Pre-Impact & Post-Impact Blazing Golden Plasma Energy Trail (matching the reference image!)
        val trailAlpha = if (progress < impactMoment) {
            (progress / impactMoment).coerceIn(0.2f, 1f)
        } else {
            (1f - ((progress - impactMoment) / 0.56f)).coerceIn(0f, 1f)
        }

        if (trailAlpha > 0.01f) {
            val currentAttackerTip = Offset(
                x = startCenter.x + (endCenter.x - startCenter.x) * easedDash,
                y = startCenter.y + (endCenter.y - startCenter.y) * easedDash
            )
            val dx = endCenter.x - startCenter.x
            val dy = endCenter.y - startCenter.y
            val perpX = -dy * 0.22f
            val perpY = dx * 0.22f

            for (strand in -1..1) {
                val arcPath = Path().apply {
                    moveTo(startCenter.x, startCenter.y)
                    val ctrlX = (startCenter.x + currentAttackerTip.x) * 0.5f + perpX * strand * 0.75f
                    val ctrlY = (startCenter.y + currentAttackerTip.y) * 0.5f + perpY * strand * 0.75f - tileSize * 0.25f
                    quadraticTo(ctrlX, ctrlY, currentAttackerTip.x, currentAttackerTip.y)
                }
                // Outer fiery amber glow
                drawPath(
                    path = arcPath,
                    color = Color(0xFFF59E0B).copy(alpha = 0.48f * trailAlpha),
                    style = Stroke(width = tileSize * 0.17f, cap = StrokeCap.Round)
                )
                // Mid golden energy
                drawPath(
                    path = arcPath,
                    color = Color(0xFFFDE047).copy(alpha = 0.84f * trailAlpha),
                    style = Stroke(width = tileSize * 0.07f, cap = StrokeCap.Round)
                )
                // Hot white-gold plasma core
                drawPath(
                    path = arcPath,
                    color = Color.White.copy(alpha = 0.96f * trailAlpha),
                    style = Stroke(width = tileSize * 0.026f, cap = StrokeCap.Round)
                )
            }
        }

        // 2. Victim Piece Pre-Impact Tremble OR Post-Impact Violent Shatter & Fly-Off
        if (progress < impactMoment) {
            val jitter = sin(progress * 95f) * (tileSize * 0.045f)
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.70f),
                radius = tileSize * 0.46f * (0.85f + 0.15f * sin(progress * 42f)),
                center = victimCenter,
                style = Stroke(width = tileSize * 0.05f)
            )
            draw3DChessPiece(
                type = event.victim.type,
                color = event.victim.color,
                center = Offset(victimCenter.x + jitter, victimCenter.y),
                pieceSize = tileSize * 0.90f,
                isInCheck = true
            )
        } else {
            val postT = ((progress - impactMoment) / (1f - impactMoment)).coerceIn(0f, 1f)
            val fadeAlpha = (1f - postT * postT).coerceIn(0f, 1f)

            // A) Blinding Radial Impact Explosion Flash
            if (postT < 0.46f) {
                val flashT = postT / 0.46f
                val flashRadius = tileSize * (0.45f + 2.25f * flashT)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (1f - flashT)),
                            Color(0xFFFDE047).copy(alpha = 0.88f * (1f - flashT)),
                            Color(0xFFF97316).copy(alpha = 0.58f * (1f - flashT)),
                            Color.Transparent
                        ),
                        center = victimCenter,
                        radius = flashRadius
                    ),
                    radius = flashRadius,
                    center = victimCenter
                )
            }

            // B) Dual Expanding Shockwave Rings
            val ring1Radius = tileSize * (0.35f + 2.75f * postT)
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = (1f - postT) * 0.88f),
                radius = ring1Radius,
                center = victimCenter,
                style = Stroke(width = tileSize * 0.085f * (1f - postT))
            )
            val ring2Radius = tileSize * (0.20f + 1.95f * postT)
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = (1f - postT) * 0.72f),
                radius = ring2Radius,
                center = victimCenter,
                style = Stroke(width = tileSize * 0.048f * (1f - postT))
            )

            // C) Victim Piece Aggressively Spins & Flies Off the Board!
            val dirX = if (endCenter.x >= startCenter.x) 1f else -1f
            val flyX = victimCenter.x + dirX * tileSize * 2.95f * postT
            val flyY = victimCenter.y - tileSize * 1.45f * sin(postT * PI.toFloat()) + tileSize * 1.15f * postT * postT
            val spinDegrees = postT * 780f * dirX
            val scaleVictim = (1f - postT * 0.48f).coerceAtLeast(0.2f)

            draw3DChessPiece(
                type = event.victim.type,
                color = event.victim.color,
                center = Offset(flyX, flyY),
                pieceSize = tileSize * 0.88f,
                elevationScale = scaleVictim,
                alpha = fadeAlpha,
                rotationDegrees = spinDegrees
            )

            // D) 54 3D Shattered Obsidian/Marble Shards & Glowing Fiery Sparks Bursting Outward
            val isVictimBlack = event.victim.color == PieceColor.BLACK
            val primaryShardColor = if (isVictimBlack) Color(0xFF1E293B) else Color(0xFFF5EFE6)
            val shardRimColor = if (isVictimBlack) Color(0xFFF59E0B) else Color(0xFFEF4444)

            for (shard in shards) {
                val dist = tileSize * 2.65f * shard.speedFactor * postT
                val sx = victimCenter.x + cos(shard.angleRad) * dist
                val sy = victimCenter.y + sin(shard.angleRad) * dist + (tileSize * 1.35f * shard.gravityFactor * postT * postT)

                if (shard.isGlowingEmber) {
                    val tailDist = (dist - tileSize * 0.40f * (1f - postT)).coerceAtLeast(0f)
                    val tx = victimCenter.x + cos(shard.angleRad) * tailDist
                    val ty = victimCenter.y + sin(shard.angleRad) * tailDist + (tileSize * 1.35f * shard.gravityFactor * postT * postT * 0.85f)

                    drawLine(
                        color = Color(0xFFF97316).copy(alpha = fadeAlpha * 0.92f),
                        start = Offset(tx, ty),
                        end = Offset(sx, sy),
                        strokeWidth = tileSize * 0.048f * (1f - postT * 0.5f),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color(0xFFFDE047).copy(alpha = fadeAlpha),
                        radius = tileSize * 0.038f * (1f - postT * 0.4f),
                        center = Offset(sx, sy)
                    )
                } else {
                    val shardRadius = tileSize * shard.sizeFactor * (1f - postT * 0.35f)
                    rotate(degrees = shard.spinSpeed * postT, pivot = Offset(sx, sy)) {
                        val polyPath = Path().apply {
                            shard.polygonPoints.forEachIndexed { i, pt ->
                                val px = sx + pt.x * shardRadius
                                val py = sy + pt.y * shardRadius
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(
                            path = polyPath,
                            color = primaryShardColor.copy(alpha = fadeAlpha)
                        )
                        drawPath(
                            path = polyPath,
                            color = shardRimColor.copy(alpha = fadeAlpha * 0.95f),
                            style = Stroke(width = (tileSize * 0.022f).coerceAtLeast(1.2f))
                        )
                    }
                }
            }
        }

        // 3. Attacking Piece Charging & Slamming into Target Square
        val attackerPos = if (progress < impactMoment) {
            val liftY = -tileSize * 0.44f * sin(dashProgress * PI.toFloat())
            Offset(
                x = startCenter.x + (endCenter.x - startCenter.x) * easedDash,
                y = startCenter.y + (endCenter.y - startCenter.y) * easedDash + liftY
            )
        } else {
            endCenter
        }

        val attackerScale = if (progress < impactMoment) {
            1f + 0.38f * sin(dashProgress * PI.toFloat())
        } else {
            val settleT = ((progress - impactMoment) / 0.25f).coerceIn(0f, 1f)
            1f + 0.22f * (1f - settleT)
        }

        // Glowing golden combat aura beneath the attacking piece
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFDE047).copy(alpha = 0.72f),
                    Color(0xFFF59E0B).copy(alpha = 0.36f),
                    Color.Transparent
                ),
                center = attackerPos,
                radius = tileSize * 0.68f * attackerScale
            ),
            radius = tileSize * 0.68f * attackerScale,
            center = attackerPos
        )

        draw3DChessPiece(
            type = event.attacker.type,
            color = event.attacker.color,
            center = attackerPos,
            pieceSize = tileSize * 0.92f,
            isSelected = true,
            elevationScale = attackerScale
        )
    }
}
