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
import androidx.compose.material.icons.rounded.Restaurant
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.fra.myunical.today.models.CanteenCardState
import it.fra.myunical.ui.components.ShapeBadge

/** Next meal at the canteen, in the amber of food-related things. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CanteenCard(
    state: CanteenCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state == CanteenCardState.Loading) {
                LoadingIndicator(Modifier.size(46.dp))
            } else {
                ShapeBadge(polygon = MaterialShapes.Cookie9Sided, size = 46.dp, containerColor = colors.tertiary, contentColor = colors.onTertiary) {
                    Icon(Icons.Rounded.Restaurant, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val supporting = colors.onTertiaryContainer.copy(alpha = 0.8f)
                when (state) {
                    CanteenCardState.Loading -> Text("Mensa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    CanteenCardState.Unavailable -> {
                        Text("Mensa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Menù dei prossimi giorni non ancora pubblicati.", style = MaterialTheme.typography.bodySmall, color = supporting)
                    }
                    is CanteenCardState.Available -> {
                        Text(state.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        state.highlights.forEach {
                            Text("· $it", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("${state.dishCount} piatti in menù", style = MaterialTheme.typography.bodySmall, color = supporting)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Apri il menù della mensa")
        }
    }
}

@Preview
@Composable
private fun CanteenCardPreview() {
    MaterialTheme {
        CanteenCard(
            CanteenCardState.Available("Oggi a pranzo", listOf("Pasta al pesto", "Riso e zucca", "Farro alla pescatora"), 15),
            onClick = {}
        )
    }
}
