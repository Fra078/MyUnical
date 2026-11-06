package it.fra.myunical.today.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.fra.myunical.today.R
import it.fra.myunical.today.models.TuitionStatus

@Composable
fun TuitionStatusCard(
    status: TuitionStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (status.isUpToDate) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (status.isUpToDate) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (status.isUpToDate) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (status.isUpToDate) painterResource(R.drawable.check_circle) else painterResource(R.drawable.warning),
                    contentDescription = null,
                    tint = if (status.isUpToDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (status.isUpToDate) "Tasse in regola" else "Rata in scadenza",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val detail = if (status.isUpToDate) {
                    "Nessun pagamento in sospeso per l'anno corrente."
                } else {
                    "${status.installmentDescription.orEmpty()}: €${String.format("%.2f", status.pendingAmount ?: 0.0)} entro il ${status.dueDate.orEmpty()}"
                }

                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Icon(
                painter = painterResource(R.drawable.open_in_new),
                contentDescription = "Details",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}