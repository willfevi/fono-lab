package com.healthcare.fonolab.feature.games.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.theme.FonoBorder
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimarySoft
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.core.designsystem.theme.MockupBlueCard
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPeachCard
import com.healthcare.fonolab.core.designsystem.theme.MockupYellowCard
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.feature.games.advanced.MagicMouthRound
import com.healthcare.fonolab.feature.games.advanced.RhymeDetectiveRound
import com.healthcare.fonolab.feature.games.advanced.SentenceDetectiveRound
import com.healthcare.fonolab.feature.games.advanced.SoundMemoryRound
import com.healthcare.fonolab.feature.games.advanced.WordMarketRound
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterRound
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntRound
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionRound
import com.healthcare.fonolab.feature.games.storysequence.StoryScene
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceRound
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryRound

@Composable
fun ClinicianCueSelectorBar(
    selectedCue: CueLevel,
    onCueSelected: (CueLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val cueItems = listOf(
        CueLevel.NONE to "Independente",
        CueLevel.VISUAL to "Pista Visual",
        CueLevel.GESTURAL to "Pista Gestual",
        CueLevel.VERBAL to "Pista Verbal",
        CueLevel.MODELING to "Modelo Direto"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pista clínica:",
            style = MaterialTheme.typography.labelMedium,
            color = FonoTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        cueItems.forEach { (cue, label) ->
            val selected = selectedCue == cue
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MockupDarkPill else FonoPrimarySoft)
                    .clickable { onCueSelected(cue) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else FonoTextPrimary
                )
            }
        }
    }
}

@Composable
fun SoundHuntGameView(
    round: SoundHuntRound,
    onOptionSelected: (String) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = round.instructionText,
                    style = MaterialTheme.typography.titleLarge,
                    color = FonoTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSpeak) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ouvir instrução",
                        tint = FonoPrimary
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            round.options.forEach { option ->
                FonoCard(
                    onClick = { onOptionSelected(option.word) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(FonoPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = option.emoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = option.word,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FonoTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SpaceMissionGameView(
    round: SpaceMissionRound,
    onTargetTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = round.instruction,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = FonoTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            round.targets.forEach { target ->
                FonoCard(
                    onClick = { onTargetTapped(target.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(FonoPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = target.emoji, fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = target.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = FonoTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorySequenceGameView(
    round: StorySequenceRound,
    scenes: List<StoryScene>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onConfirmOrder: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = round.storyTitle,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = FonoTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Organize as cenas na sequência correta:",
            style = MaterialTheme.typography.bodyMedium,
            color = FonoTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            scenes.forEachIndexed { index, scene ->
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FonoPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}º",
                                fontWeight = FontWeight.Bold,
                                color = FonoPrimaryDark,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = scene.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = scene.title, fontWeight = FontWeight.SemiBold, color = FonoTextPrimary)
                            Text(text = scene.description, style = MaterialTheme.typography.bodySmall, color = FonoTextSecondary)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (index > 0) {
                                OutlinedButton(
                                    onClick = { onMoveUp(index) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, FonoBorder)
                                ) { Text("▲", color = FonoPrimary) }
                            }
                            if (index < scenes.size - 1) {
                                OutlinedButton(
                                    onClick = { onMoveDown(index) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, FonoBorder)
                                ) { Text("▼", color = FonoPrimary) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        FonoPrimaryButton(
            text = "Confirmar Sequência da História",
            onClick = { onConfirmOrder(scenes.map { it.id }) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HungryMonsterGameView(
    round: HungryMonsterRound,
    onRate: (ClinicianRating, CueLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👾", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = round.stimulus.prompt,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = FonoTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            FonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = round.stimulus.emoji, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = round.stimulus.word,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = FonoPrimaryDark
                    )
                }
            }
        }

        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Avaliação Clínica da Produção",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = FonoTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRate(ClinicianRating.CORRECT_INDEPENDENT, CueLevel.NONE) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FonoSecondary)
                    ) {
                        Text("Correta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onRate(ClinicianRating.CORRECT_WITH_CUE, CueLevel.VERBAL) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FonoPrimary)
                    ) {
                        Text("C/ Pista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onRate(ClinicianRating.INCORRECT, CueLevel.NONE) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Incorreta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onRate(ClinicianRating.NO_RESPONSE, CueLevel.NONE) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FonoBorder)
                    ) {
                        Text("S/ Resp", fontSize = 11.sp, color = FonoTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun WordFactoryGameView(
    round: WordFactoryRound,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(FonoPrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏭", fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = round.promptText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = FonoTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            FonoCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = round.baseEmoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = round.baseWord,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = FonoPrimaryDark
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            round.options.forEach { option ->
                FonoCard(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FonoTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// NEW VIEWS FOR GAMES 6 TO 10
// ============================================================================

@Composable
fun RhymeDetectiveGameView(
    round: RhymeDetectiveRound,
    onOptionSelected: (String) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupYellowCard) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🕵️ Pista de Rima", fontWeight = FontWeight.Bold, color = MockupDarkPill)
                    IconButton(onClick = onSpeak) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Ouvir", tint = MockupDarkPill)
                    }
                }
                Text(round.clueEmoji, fontSize = 52.sp)
                Text(round.clueWord, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MockupDarkPill)
                Spacer(modifier = Modifier.height(6.dp))
                Text(round.promptText, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MockupDarkPill)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            round.options.forEach { opt ->
                FonoCard(
                    onClick = { onOptionSelected(opt.word) },
                    modifier = Modifier.fillMaxWidth().height(74.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opt.emoji, fontSize = 30.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(opt.word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FonoTextPrimary)
                            Text("Terminação: ${opt.endingSound}", style = MaterialTheme.typography.bodySmall, color = FonoTextSecondary)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MagicMouthGameView(
    round: MagicMouthRound,
    selectedCue: CueLevel,
    onRate: (ClinicianRating, CueLevel) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupPeachCard) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👄 ${round.articulatoryCueTitle}", fontWeight = FontWeight.Bold, color = MockupDarkPill)
                    IconButton(onClick = onSpeak) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Ouvir dica", tint = MockupDarkPill)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(round.emoji, fontSize = 56.sp)
                Text(
                    text = "${round.targetWord} (${round.targetPhoneme})",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MockupDarkPill
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = round.articulatoryTip,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MockupDarkPill
                )
            }
        }

        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro Fonético da Produção (Pista ativa: ${selectedCue.name})",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = FonoTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRate(ClinicianRating.CORRECT_INDEPENDENT, CueLevel.NONE) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FonoSecondary)
                    ) {
                        Text("Adequada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val cue = if (selectedCue == CueLevel.NONE) CueLevel.VISUAL else selectedCue
                            onRate(ClinicianRating.CORRECT_WITH_CUE, cue)
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FonoPrimary)
                    ) {
                        Text("Com Pista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onRate(ClinicianRating.INCORRECT, selectedCue) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Troca/Omissão", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WordMarketGameView(
    round: WordMarketRound,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupGreenCard) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🛒 Setor: ${round.targetCategory}", fontWeight = FontWeight.Bold, color = MockupDarkPill)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = round.instruction,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MockupDarkPill
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            round.options.forEach { item ->
                FonoCard(
                    onClick = { onOptionSelected(item.word) },
                    modifier = Modifier.fillMaxWidth().height(76.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FonoTextPrimary
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SentenceDetectiveGameView(
    round: SentenceDetectiveRound,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupBlueCard) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧩 Complete a Frase", fontWeight = FontWeight.Bold, color = MockupDarkPill)
                Spacer(modifier = Modifier.height(10.dp))
                Text(round.sceneEmoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = round.sentenceFrame,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MockupDarkPill
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            round.options.forEach { part ->
                FonoCard(
                    onClick = { onOptionSelected(part) },
                    modifier = Modifier.fillMaxWidth().height(68.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = part,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FonoTextPrimary
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SoundMemoryGameView(
    round: SoundMemoryRound,
    onOptionSelected: (String) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔔 Memória Auditiva", fontWeight = FontWeight.Bold, color = FonoPrimaryDark)
                    IconButton(onClick = onSpeak) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Ouvir sequência", tint = FonoPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = round.auditoryPrompt,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = FonoTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            round.options.forEach { opt ->
                FonoCard(
                    onClick = { onOptionSelected(opt.id) },
                    modifier = Modifier.fillMaxWidth().height(76.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opt.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(opt.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FonoTextPrimary)
                            Text(opt.onomatopoeia, style = MaterialTheme.typography.bodySmall, color = FonoTextSecondary)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
