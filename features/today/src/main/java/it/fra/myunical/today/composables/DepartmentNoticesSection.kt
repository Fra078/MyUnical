package it.fra.myunical.today.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.fra.myunical.today.models.DepartmentNotices
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.ui.components.GroupedListItem

/** The latest notices of the student's department, with a link to the full list. */
@Composable
fun DepartmentNoticesSection(
    notices: DepartmentNotices,
    onNoticeClick: (NoticeUiModel) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Avvisi ${notices.department}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAllClick) {
                Text("Vedi tutti")
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp)
                )
            }
        }
        notices.items.forEachIndexed { index, notice ->
            GroupedListItem(
                title = notice.title,
                supporting = listOf(notice.category, notice.date).filter { it.isNotBlank() }.joinToString(" · "),
                index = index,
                count = notices.items.size,
                onClick = { onNoticeClick(notice) }
            )
        }
    }
}
