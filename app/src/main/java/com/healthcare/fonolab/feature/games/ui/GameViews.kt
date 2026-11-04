package com.healthcare.fonolab.feature.games.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondaryContainer
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterRound
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntRound
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionRound
import com.healthcare.fonolab.feature.games.storysequence.StoryScene
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceRound
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryRound

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

        // Tactile Choice Cards
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
        FonoCard(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                FonoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
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

        // Clinician Rapid Rating Controls
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
            FonoCard(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
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

        // Options blocks
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

