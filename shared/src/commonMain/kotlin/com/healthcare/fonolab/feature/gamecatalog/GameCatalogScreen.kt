package com.healthcare.fonolab.feature.gamecatalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBlueCard
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPinkCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.core.designsystem.theme.MockupYellowCard
import com.healthcare.fonolab.domain.game.GameDefinition
import com.healthcare.fonolab.feature.games.GameCatalogRegistry

@Composable
fun GameCatalogScreen(
    onPlayGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val games = GameCatalogRegistry.games

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Catálogo de Jogos",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Minijogos terapêuticos e educativos estruturados",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(games) { index, game ->
                GameCatalogCard(
                    game = game,
                    index = index,
                    onPlayClick = { onPlayGame(game.id) }
                )
            }
        }
    }
}

@Composable
private fun GameCatalogCard(
    game: GameDefinition,
    index: Int,
    onPlayClick: () -> Unit
) {
    val pastelColor = when (index % 5) {
        0 -> MockupYellowCard
        1 -> MockupBlueCard
        2 -> MockupPurpleHero
        3 -> MockupGreenCard
        else -> MockupPinkCard
    }

    FonoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Icon Bubble in pastel
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(pastelColor.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = game.iconEmoji, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.clinicalArea.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MockupTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MockupTextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FonoStatusBadge(
                    text = "Faixa: ${game.indicativeAgeRange}",
                    containerColor = pastelColor.copy(alpha = 0.35f),
                    contentColor = MockupDarkPill
                )
                FonoStatusBadge(
                    text = "~${game.estimatedDurationMinutes} min",
                    containerColor = MockupBackground,
                    contentColor = MockupTextDark
                )
                FonoStatusBadge(
                    text = "${game.targetSkills.size} Habilidades",
                    containerColor = MockupBackground,
                    contentColor = MockupTextDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            FonoPrimaryButton(
                text = "Jogar (Prática Livre)",
                onClick = onPlayClick,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

