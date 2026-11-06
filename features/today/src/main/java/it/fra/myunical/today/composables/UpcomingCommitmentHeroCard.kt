package it.fra.myunical.today.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import it.fra.myunical.today.models.CommitmentType
import it.fra.myunical.today.models.UpcomingCommitment
import it.fra.myunical.today.models.UpcomingCommitmentState

private val CardShape = RoundedCornerShape(28.dp)

@Composable
fun UpcomingCommitmentHeroCard(
    state: UpcomingCommitmentState,
    onOpenSchedule: () -> Unit,
    onSetupSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state,
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "UpcomingCommitment",
        modifier = modifier.fillMaxWidth()
    ) { target ->
        when (target) {
            UpcomingCommitmentState.Loading -> LoadingCard()
            UpcomingCommitmentState.NotConfigured -> SetupScheduleCard(onSetupSchedule)
            UpcomingCommitmentState.NothingScheduled -> InfoCard(
                icon = Icons.Rounded.EventAvailable,
                title = "Nessuna lezione in programma",
                description = "Non ci sono lezioni nelle prossime settimane.",
                onClick = onOpenSchedule
            )
            UpcomingCommitmentState.Unavailable -> InfoCard(
                icon = Icons.Rounded.CloudOff,
                title = "Orario non disponibile",
                description = "Riproveremo a breve. Tocca per aprire l'orario.",
                onClick = onOpenSchedule
            )
            is UpcomingCommitmentState.Available -> CommitmentCard(target.commitment, onOpenSchedule)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CommitmentCard(commitment: UpcomingCommitment, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container = if (commitment.isOngoing) colors.tertiaryContainer else colors.primaryContainer
    val content = if (commitment.isOngoing) colors.onTertiaryContainer else colors.onPrimaryContainer

    Surface(
        onClick = onClick,
        shape = CardShape,
        color = container,
        contentColor = content,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.85f)) {
                    Text(
                        text = when {
                            commitment.isOngoing -> "In corso"
                            commitment.type == CommitmentType.EXAM -> "Prossimo appello"
                            else -> "Prossima lezione"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = commitment.relativeTime,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = commitment.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            commitment.progress?.let { progress ->
                LinearWavyProgressIndicator(
                    progress = { progress },
                    color = content,
                    trackColor = content.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Detail(Icons.Rounded.Schedule, commitment.timeSlot)
                commitment.room?.let { Detail(Icons.Rounded.Place, it) }
                commitment.teachers?.let { Detail(Icons.Rounded.Person, it) }
            }

            if (commitment.followedBy != null || commitment.scheduleName != null) {
                HorizontalDivider(color = content.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = commitment.followedBy ?: commitment.scheduleName.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Apri l'orario",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Detail(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetupScheduleCard(onSetupSchedule: () -> Unit) {
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShapeIcon(Icons.Rounded.CalendarMonth, MaterialShapes.Cookie9Sided, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Configura il tuo orario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Scegli corso e insegnamenti per vedere qui la tua prossima lezione.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onSetupSchedule) { Text("Configura") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InfoCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShapeIcon(icon, MaterialShapes.Cookie6Sided, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, CardShape),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(Modifier.size(48.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapeIcon(icon: ImageVector, polygon: RoundedPolygon, container: Color, content: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(container, polygon.toShape()),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = content)
    }
}


@Preview
@Composable
private fun UpcomingCommitmentHeroCardPreview() {
    MaterialTheme {
        UpcomingCommitmentHeroCard(
            state = UpcomingCommitmentState.Available(
                UpcomingCommitment(
                    title = "Ingegneria del Software",
                    type = CommitmentType.LESSON,
                    timeSlot = "09:30 – 11:30",
                    room = "MT4, Cubo 21B",
                    relativeTime = "Termina tra 25 min",
                    teachers = "ROSSI Mario",
                    progress = 0.6f,
                    followedBy = "Poi alle 11:30 · Basi di Dati"
                )
            ),
            onOpenSchedule = {},
            onSetupSchedule = {}
        )
    }
}
