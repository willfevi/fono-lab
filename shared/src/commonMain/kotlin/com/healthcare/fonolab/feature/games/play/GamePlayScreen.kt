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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.PastelMetricCard
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimarySoft
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.core.designsystem.theme.MockupCyanCard
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPeachCard
import com.healthcare.fonolab.feature.games.GameCatalogRegistry
import com.healthcare.fonolab.feature.games.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    viewModel: GamePlayViewModel,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.totalRounds == 0) {
        FonoLoadingScreen(message = "Preparando jogo terapêutico...")
        return
    }

    if (state.isGameOver) {
        GameOverView(
            xp = state.xpEarned,
            clinicalAccuracyPercent = state.clinicalAccuracyPercent,
            correctCount = state.correctCount,
            totalRounds = state.totalRounds,
            cuedCount = state.cuedCount,
            onFinish = onExitGame
        )
        return
    }

    val gameDef = GameCatalogRegistry.getById(state.gameId)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.navigationBars),
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                title = {
                    Column {
                        Text(
                            text = gameDef?.title ?: "Jogo Terapêutico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FonoTextPrimary
                        )
                        Text(
                            text = "Rodada ${state.currentRoundIndex + 1} de ${state.totalRounds}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FonoTextSecondary
                        )
                    }
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
                .consumeWindowInsets(padding)
        ) {
            LinearProgressIndicator(
                progress = { (state.currentRoundIndex.toFloat() / state.totalRounds) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = FonoPrimary,
                trackColor = FonoPrimaryLight
            )

            // Clinician Trial-by-Trial Cue Hierarchy Bar
            ClinicianCueSelectorBar(
                selectedCue = state.selectedCue,
                onCueSelected = viewModel::onCueSelected
            )

            Box(modifier = Modifier.weight(1f)) {
                when (state.gameId) {
                    "sound_hunt" -> state.soundHuntRound?.let { round ->
                        SoundHuntGameView(
                            round = round,
                            onOptionSelected = viewModel::onSoundHuntAnswer,
                            onSpeak = { viewModel.speakInstruction(round.instructionText) }
                        )
                    }
                    "space_mission" -> state.spaceRound?.let { round ->
                        SpaceMissionGameView(
                            round = round,
                            onTargetTapped = viewModel::onSpaceTargetTapped
                        )
                    }
                    "story_sequence" -> state.storyRound?.let { round ->
                        StorySequenceGameView(
                            round = round,
                            scenes = state.storyScenes,
                            onMoveUp = viewModel::moveStorySceneUp,
                            onMoveDown = viewModel::moveStorySceneDown,
                            onConfirmOrder = viewModel::onStoryConfirm
                        )
                    }
                    "hungry_monster" -> state.monsterRound?.let { round ->
                        HungryMonsterGameView(
                            round = round,
                            onRate = viewModel::onMonsterRate
                        )
                    }
                    "word_factory" -> state.wordFactoryRound?.let { round ->
                        WordFactoryGameView(
                            round = round,
                            onOptionSelected = viewModel::onFactoryOptionSelected
                        )
                    }
                    "rhyme_detective" -> state.rhymeRound?.let { round ->
                        RhymeDetectiveGameView(
                            round = round,
                            onOptionSelected = viewModel::onRhymeOptionSelected,
                            onSpeak = { viewModel.speakInstruction(round.promptText) }
                        )
                    }
                    "magic_mouth" -> state.magicMouthRound?.let { round ->
                        MagicMouthGameView(
                            round = round,
                            selectedCue = state.selectedCue,
                            onRate = viewModel::onMagicMouthRate,
                            onSpeak = { viewModel.speakInstruction(round.articulatoryTip) }
                        )
                    }
                    "word_market" -> state.wordMarketRound?.let { round ->
                        WordMarketGameView(
                            round = round,
                            onOptionSelected = viewModel::onWordMarketOptionSelected
                        )
                    }
                    "sentence_detective" -> state.sentenceRound?.let { round ->
                        SentenceDetectiveGameView(
                            round = round,
                            onOptionSelected = viewModel::onSentenceOptionSelected
                        )
                    }
                    "sound_memory" -> state.soundMemoryRound?.let { round ->
                        SoundMemoryGameView(
                            round = round,
                            onOptionSelected = viewModel::onSoundMemoryOptionSelected,
                            onSpeak = { viewModel.speakInstruction(round.spokenCue) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverView(
    xp: Int,
    clinicalAccuracyPercent: Int,
    correctCount: Int,
    totalRounds: Int,
    cuedCount: Int,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(FonoPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎉", fontSize = 38.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Atividade Concluída!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = FonoTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pontuação lúdica e dados clínicos registrados separadamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FonoTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Separation of Ludic Reward vs Clinical Accuracy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PastelMetricCard(
                        label = "Acurácia Clínica",
                        value = "$clinicalAccuracyPercent%",
                        unit = "$correctCount/$totalRounds acertos",
                        containerColor = MockupGreenCard,
                        modifier = Modifier.weight(1f)
                    )
                    PastelMetricCard(
                        label = "Com Pista",
                        value = cuedCount.toString(),
                        unit = "tentativas",
                        containerColor = MockupCyanCard,
                        modifier = Modifier.weight(1f)
                    )
                    PastelMetricCard(
                        label = "Bônus Lúdico",
                        value = "+$xp",
                        unit = "XP estrelas",
                        containerColor = MockupPeachCard,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                FonoPrimaryButton(
                    text = "Concluir e Voltar",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
