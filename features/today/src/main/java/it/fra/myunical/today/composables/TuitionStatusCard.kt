package it.fra.myunical.today.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import it.fra.myunical.today.models.TuitionState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TuitionStatusCard(
    state: TuitionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val success = MaterialTheme.extendedColors.success
    val warning = MaterialTheme.extendedColors.warning
    // Paid → green, to pay → orange, overdue → red
    val (container, content) = when {
        state is TuitionState.Due && state.isOverdue -> colors.errorContainer to colors.onErrorContainer
        state is TuitionState.Due -> warning.container to warning.onContainer
        state == TuitionState.UpToDate -> success.container to success.onContainer
        else -> colors.surfaceContainerLow to colors.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (state) {
                TuitionState.Loading -> LoadingIndicator(Modifier.size(46.dp))
                TuitionState.Unavailable -> Badge(Icons.Rounded.CloudOff, MaterialShapes.Cookie6Sided, colors.surfaceContainerHighest, colors.onSurfaceVariant)
                TuitionState.UpToDate -> Badge(Icons.Rounded.Verified, MaterialShapes.Cookie9Sided, success.color, success.onColor)
                is TuitionState.Due -> if (state.isOverdue) {
                    Badge(Icons.Rounded.PriorityHigh, MaterialShapes.SoftBurst, colors.error, colors.onError)
                } else {
                    Badge(Icons.Rounded.Schedule, MaterialShapes.Sunny, warning.color, warning.onColor)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val (title, detail) = when (state) {
                    TuitionState.Loading -> "Tasse" to "Verifica dei pagamenti in corso…"
                    TuitionState.Unavailable -> "Tasse" to "Stato dei pagamenti non disponibile al momento."
                    TuitionState.UpToDate -> "Tasse in regola" to "Nessun pagamento in sospeso."
                    is TuitionState.Due -> {
                        val title = if (state.isOverdue) "Pagamento scaduto" else "Tasse da pagare"
                        val count = if (state.pendingCount == 1) "1 addebito" else "${state.pendingCount} addebiti"
                        title to listOfNotNull("${state.amount} · $count", state.dueLabel).joinToString(", ")
                    }
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.8f))
            }

            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Apri le tasse")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Badge(icon: ImageVector, polygon: RoundedPolygon, container: Color, content: Color) {
    ShapeBadge(polygon = polygon, size = 46.dp, containerColor = container, contentColor = content) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@Preview
@Composable
private fun TuitionStatusCardPreview() {
    MaterialTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TuitionStatusCard(TuitionState.UpToDate, onClick = {})
            TuitionStatusCard(TuitionState.Due("148,50 €", "scade il 30/11", 1, isOverdue = false), onClick = {})
            TuitionStatusCard(TuitionState.Due("148,50 €", "scaduta il 30/09", 2, isOverdue = true), onClick = {})
        }
    }
}
