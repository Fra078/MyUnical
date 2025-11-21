package it.appm.myunical.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.appm.myunical.R
import it.appm.myunical.data.MyUnical
import it.appm.myunical.model.Evento
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val viewModel : ScheduleViewModel = viewModel()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.orario), maxLines = 1)
                    },
                    scrollBehavior = scrollBehavior,
                )
                CalendarBar(
                    calendarInfo = selectedMonth,
                    goBack = viewModel::goBack,
                    goForward = viewModel::goForward
                )
            }
        }
    ) { contentPadding ->
        ScheduleContent(events, isRefreshing, Modifier.padding(contentPadding)){
            viewModel.forceUpdate()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleContent(
    events: List<ScheduleViewModel.EventItem>,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    refresh: () -> Unit
){
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = refresh,
        modifier = Modifier.fillMaxSize().then(modifier)
    ) {
        LazyColumn {
            items(events){
                when(it){
                    is ScheduleViewModel.EventItem.Day -> EventTag(it)
                    is ScheduleViewModel.EventItem.Event -> EventCard(it)
                }
            }
        }
    }
}

@Composable
private fun CalendarBar(
    calendarInfo : ScheduleViewModel.CalendarInfo,
    goForward: () -> Unit,
    goBack: () -> Unit
){
    val text = buildString {
        append(stringResource(convertMonth(calendarInfo.month)))
        append(" ")
        if (calendarInfo.year != calendarInfo.currentYear)
            append(calendarInfo.year)
    }
    Surface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = goBack,
                modifier = Modifier.padding(8.dp),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null
                    )
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = goForward,
                modifier = Modifier.padding(8.dp),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            )}
    }
}

@Composable
private fun EventCard(event: ScheduleViewModel.EventItem.Event){
    val startHour = "${event.startOffset / 60}".padStart(2, '0')
    val startMinute = "${event.startOffset % 60}".padStart(2, '0')
    val endHour = "${event.endOffset / 60}".padStart(2, '0')
    val endMinute = "${event.endOffset % 60}".padStart(2, '0')

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(
                text = event.title,
                maxLines = 2,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = event.location,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$startHour:$startMinute / $endHour:$endMinute",
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun EventTag(day: ScheduleViewModel.EventItem.Day){
    Text(
        text = day.toString(),
        modifier = Modifier.padding(top = 8.dp, start = 16.dp),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    )
}

private fun convertMonth(month: Int) : Int = when(month){
    1 -> R.string.gennaio
    2 -> R.string.febbraio
    3 -> R.string.marzo
    4 -> R.string.aprile
    5 -> R.string.maggio
    6 -> R.string.giugno
    7 -> R.string.luglio
    8 -> R.string.agosto
    9 -> R.string.settembre
    10 -> R.string.ottobre
    11 -> R.string.novembre
    else -> R.string.dicembre

}