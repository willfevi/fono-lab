package com.healthcare.fonolab.feature.games.play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.theme.FonoBorder
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimarySoft
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.feature.games.ui.HungryMonsterGameView
import com.healthcare.fonolab.feature.games.ui.SoundHuntGameView
import com.healthcare.fonolab.feature.games.ui.SpaceMissionGameView
import com.healthcare.fonolab.feature.games.ui.StorySequenceGameView
import com.healthcare.fonolab.feature.games.ui.WordFactoryGameView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    viewModel: GamePlayViewModel,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.totalRounds == 0) {
        FonoLoadingScreen(message = "Preparando jogo...")
        return
    }

    if (state.isGameOver) {
        GameOverView(
            xp = state.xpEarned,
            onFinish = onExitGame
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rodada ${state.currentRoundIndex + 1} de ${state.totalRounds}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FonoTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExitGame) {
                        Icon(Icons.Default.Close, contentDescription = "Sair do jogo", tint = FonoTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { (state.currentRoundIndex.toFloat() / state.totalRounds) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = FonoPrimary,
                trackColor = FonoPrimaryLight
            )

            Box(modifier = Modifier.weight(1f)) {
                when (state.gameId) {
                    "sound_hunt" -> {
                        state.soundHuntRound?.let { round ->
                            SoundHuntGameView(
                                round = round,
                                onOptionSelected = viewModel::onSoundHuntAnswer,
                                onSpeak = { viewModel.speakInstruction(round.instructionText) }
                            )
                        }
                    }
                    "space_mission" -> {
                        state.spaceRound?.let { round ->
                            SpaceMissionGameView(
                                round = round,
                                onTargetTapped = viewModel::onSpaceTargetTapped
                            )
                        }
                    }
                    "story_sequence" -> {
                        state.storyRound?.let { round ->
                            StorySequenceGameView(
                                round = round,
                                scenes = state.storyScenes,
                                onMoveUp = viewModel::moveStorySceneUp,
                                onMoveDown = viewModel::moveStorySceneDown,
                                onConfirmOrder = viewModel::onStoryConfirm
                            )
                        }
                    }
                    "hungry_monster" -> {
                        state.monsterRound?.let { round ->
                            HungryMonsterGameView(
                                round = round,
                                onRate = viewModel::onMonsterRate
                            )
                        }
                    }
                    "word_factory" -> {
                        state.wordFactoryRound?.let { round ->
                            WordFactoryGameView(
                                round = round,
                                onOptionSelected = viewModel::onFactoryOptionSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverView(
    xp: Int,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        FonoCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FonoPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎉", fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Atividade Concluída!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = FonoTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Excelente! O paciente completou todas as rodadas com sucesso.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = FonoTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FonoPrimarySoft)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "+$xp Pontos de Experiência ⭐",
                        fontWeight = FontWeight.Bold,
                        color = FonoPrimaryDark
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                FonoPrimaryButton(
                    text = "Concluir e Voltar",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

