package com.healthcare.fonolab.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthcare.fonolab.core.designsystem.theme.FonoBorder
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryGradient
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimarySoft
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBlueCard
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupCyanCard
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPeachCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHeroEnd
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.core.designsystem.theme.MockupYellowCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Floating Dark Capsule Pill Navigation Bar identical to the mockup.
 */
@Composable
fun MockupFloatingNavBar(
    items: List<Pair<ImageVector, String>>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(36.dp)

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .shadow(
                elevation = 16.dp,
                shape = pillShape,
                spotColor = Color(0x66000000),
                ambientColor = Color(0x33000000)
            ),
        shape = pillShape,
        color = MockupDarkPill,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, pair ->
                val isSelected = index == selectedIndex
                val (icon, description) = pair
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onItemSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        tint = if (isSelected) MockupDarkPill else Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mockup Top Header: Avatar circle on left, Greeting & Date in center, circular action on right.
 */
@Composable
fun MockupHeader(
    title: String,
    subtitle: String,
    avatarInitial: String = "F",
    actionIcon: ImageVector = Icons.Default.Search,
    onAvatarClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clinician Avatar Circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MockupPurpleHero.copy(alpha = 0.35f))
                .border(1.5.dp, Color.White, CircleShape)
                .clickable(enabled = onAvatarClick != null) { onAvatarClick?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MockupDarkPill
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MockupTextMuted
            )
        }

        // Circular Outlined Icon Action Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MockupSurface)
                .border(1.dp, MockupBorder, CircleShape)
                .clickable(enabled = onActionClick != null) { onActionClick?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = MockupTextDark,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * Mockup Hero Card ("Daily Challenge" style lavender banner with stacked avatars and 3D visual).
 */
@Composable
fun MockupHeroChallengeCard(
    tag: String = "DESAFIO DO DIA",
    title: String = "Realize os planos fonoaudiológicos",
    subtitle: String = "Atividades prioritárias com pacientes agendados",
    participantInitials: List<String> = listOf("A", "B", "C"),
    extraCount: Int = 4,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MockupPurpleHero, MockupPurpleHeroEnd)
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                // Top Tag Pill
                Surface(
                    color = Color.White.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = tag.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MockupDarkPill,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MockupDarkPill,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupDarkPill.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Row: Overlapping Avatars Stack & 3D Sparkle Decorative Circle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-10).dp)
                    ) {
                        participantInitials.forEachIndexed { i, initial ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (i % 3) {
                                            0 -> MockupYellowCard
                                            1 -> MockupBlueCard
                                            else -> MockupGreenCard
                                        }
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupDarkPill
                                )
                            }
                        }

                        if (extraCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extraCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupDarkPill
                                )
                            }
                        }
                    }

                    // 3D tactile icon badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MockupDarkPill,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mockup Horizontal Calendar 7-Day Strip with Obsidian Pill Active Indicator.
 */
@Composable
fun CalendarWeekStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine the week window around selectedDate (e.g., current week Monday to Sunday)
    val dayOfWeek = selectedDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val startOfWeek = selectedDate.minusDays((dayOfWeek - 1).toLong())
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val ptBr = Locale.forLanguageTag("pt-BR")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MockupSurface,
        border = BorderStroke(1.dp, MockupBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEach { date ->
                val isSelected = date == selectedDate
                val dayShort = date.dayOfWeek.getDisplayName(TextStyle.SHORT, ptBr)
                    .take(3)
                    .replaceFirstChar { it.uppercase() }
                val dayNumber = date.dayOfMonth.toString()

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MockupDarkPill else Color.Transparent)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onDateSelected(date) }
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayShort,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MockupTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNumber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MockupTextDark
                        )
                        if (!isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MockupBorder)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pastel Activity Card (Yellow, Sky Blue, etc. with tag pill, details and bottom avatar + CTA).
 */
@Composable
fun PastelActivityCard(
    tag: String,
    title: String,
    details: String,
    participantName: String? = null,
    participantInitial: String? = null,
    containerColor: Color = MockupYellowCard,
    textColor: Color = MockupTextDark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Tag Pill
            Surface(
                color = Color.White.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = textColor,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (participantName != null && participantInitial != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participantInitial,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = participantName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Circular action button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MockupDarkPill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mockup Trio Pastel Metric Card (Mint Green, Sky Cyan, Peach from Profile screen).
 */
@Composable
fun PastelMetricCard(
    label: String,
    value: String,
    unit: String? = null,
    containerColor: Color = MockupGreenCard,
    textColor: Color = MockupTextDark,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mockup List Item: Clean white card with circular pastel icon, title, subtitle & chevron.
 */
@Composable
fun MockupListItem(
    title: String,
    subtitle: String,
    leadingText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingBgColor: Color = MockupPurpleHero.copy(alpha = 0.25f),
    leadingTextColor: Color = MockupDarkPill,
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MockupSurface,
        border = BorderStroke(1.dp, MockupBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular leading container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(leadingBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (leadingText != null) {
                    Text(
                        text = leadingText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = leadingTextColor
                    )
                } else if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = leadingTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MockupTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupTextMuted
                )
            }

            if (badgeText != null) {
                FonoStatusBadge(text = badgeText)
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Trailing Chevron Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MockupBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MockupTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Clean clinical card with pure white background, delicate border, and soft elevation.
 */
@Composable
fun FonoCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MockupSurface,
    border: BorderStroke? = BorderStroke(1.dp, MockupBorder),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            )
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            content()
        }
    }
}

/**
 * Primary CTA Button with gentle lilac gradient, rounded corners (16.dp), tactile feedback.
 */
@Composable
fun FonoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(52.dp)
            .then(
                if (enabled) {
                    Modifier
                        .shadow(elevation = 2.dp, shape = shape, spotColor = MockupDarkPill.copy(alpha = 0.25f))
                        .clip(shape)
                        .background(MockupDarkPill)
                } else {
                    Modifier
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                }
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Secondary outlined/soft button for alternate actions.
 */
@Composable
fun FonoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MockupBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MockupDarkPill,
            containerColor = MockupSurface
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Clinical status badge chip with soft container and high contrast label.
 */
@Composable
fun FonoStatusBadge(
    text: String,
    containerColor: Color = MockupPurpleHero.copy(alpha = 0.25f),
    contentColor: Color = MockupDarkPill,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun FonoEmptyState(
    icon: ImageVector? = null,
    emoji: String? = null,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MockupPurpleHero.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MockupDarkPill
                    )
                } else if (emoji != null) {
                    Text(text = emoji, fontSize = 32.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MockupTextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MockupTextMuted,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                FonoPrimaryButton(
                    text = actionLabel,
                    onClick = onActionClick
                )
            }
        }
    }
}

@Composable
fun FonoLoadingScreen(
    message: String = "Carregando...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MockupDarkPill,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MockupTextMuted
            )
        }
    }
}

