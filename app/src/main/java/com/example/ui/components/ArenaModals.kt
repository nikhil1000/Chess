package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MatchRecordEntity
import com.example.model.PendingPromotion
import com.example.model.PieceType
import com.example.ui.theme.ArenaBorderSubtle
import com.example.ui.theme.ArenaCardSurface
import com.example.ui.theme.ArenaNeonBlue
import com.example.ui.theme.ArenaObsidianBg
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryIvory
import com.example.ui.theme.TextSecondarySlate

@Composable
fun PawnPromotionModal(
    pendingPromotion: PendingPromotion,
    onSelectType: (PieceType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xF50B101B),
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(RoyalGoldLight, RoyalGoldDark, RoyalGoldLight)
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(18.dp))
                .testTag("pawn_promotion_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldenCrownIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pawn Promotion",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CinzelFontFamily,
                                color = TextPrimaryIvory,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Promotion",
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Choose a piece to promote your pawn",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondarySlate
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (option in PieceType.promotionOptions) {
                        val isSelected = pendingPromotion.selectedType == option
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                RoyalGold.copy(alpha = 0.28f),
                                                ArenaCardSurface
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(ArenaCardSurface, ArenaObsidianBg)
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) RoyalGoldLight else ArenaBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectType(option) }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                .testTag("promote_option_${option.name.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ChessPiece3DIcon(
                                type = option,
                                color = pendingPromotion.pawn.color,
                                isSelected = isSelected,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSelected) RoyalGoldLight else TextSecondarySlate,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ArenaObsidianBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(46.dp)
                        .testTag("confirm_promotion_button")
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ArenaObsidianBg,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MatchArchivesModal(
    records: List<MatchRecordEntity>,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ArenaCardSurface,
            border = BorderStroke(1.5.dp, RoyalGold.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 480.dp)
                .testTag("match_archives_modal")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Arena Match Archives",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CinzelFontFamily,
                                color = RoyalGoldLight
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondarySlate
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = ArenaBorderSubtle
                )

                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Completed matches are automatically chronicled here.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondarySlate,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(records, key = { it.id }) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ArenaObsidianBg, RoundedCornerShape(10.dp))
                                    .border(1.dp, ArenaBorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.resultTitle,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = RoyalGoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = item.difficultyLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ArenaNeonBlue
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Moves: ${item.totalMoves}  ·  Captures: ${item.totalCaptures}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondarySlate,
                                        fontSize = 12.sp
                                    )
                                )
                                if (item.pgnSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.pgnSummary,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = TextMutedSlate
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onClearAll,
                        border = BorderStroke(1.dp, ArenaBorderSubtle),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Archives", color = TextSecondarySlate)
                    }
                }
            }
        }
    }
}
