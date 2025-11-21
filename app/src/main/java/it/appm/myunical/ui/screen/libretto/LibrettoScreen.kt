package it.appm.myunical.ui.screen.libretto

import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.appm.myunical.R
import it.appm.myunical.data.room.LibrettoItem
import it.appm.myunical.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrettoScreen(){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    AppTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(R.string.libretto), maxLines = 1) },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { contentPadding ->
            LibrettoList(contentPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrettoList(padding: PaddingValues){
    val viewModel : LibrettoViewModel = viewModel()
    PullToRefreshBox(
        isRefreshing = viewModel.loading,
        onRefresh = { viewModel.updateLibretto() },
        modifier = Modifier.fillMaxWidth().padding(padding),
    ) {
        LazyColumn {
            item {
                Row {
                    ProgressBox(
                        title = stringResource(R.string.media),
                        value = viewModel.mediaP,
                        max = 30,
                        modifier = Modifier
                            .fillMaxWidth(1/2f)
                            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                    )
                    ProgressBox(
                        title = stringResource(R.string.cfu),
                        value = viewModel.cfu.toFloat(),
                        max = viewModel.maxCfu,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                    )
                }
                ProgressBox(
                    title = stringResource(R.string.proiezione_laurea),
                    value = viewModel.votoLaurea,
                    max = 110,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.corsi),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(18.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(viewModel.list){
                ExamCard(it)
            }
        }
    }

}

@Composable
private fun ExamCard(entity: LibrettoItem){
    val voteText = when {
        entity.vote == null && !entity.date.isNullOrBlank()-> "Id"
        entity.vote == null -> ""
        entity.laude -> entity.vote.toString() + "L"
        else -> entity.vote.toString()
    }
    val background = if(!entity.date.isNullOrBlank()) Color(0xFFcdeda3) else MaterialTheme.colorScheme.tertiaryContainer
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(background, MaterialTheme.colorScheme.onTertiaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).width(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(entity.cfu.toString() + " " + stringResource(R.string.cfu))
                Text(
                    text = entity.title,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = voteText,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun ProgressBox(
    title: String,
    value: Float,
    max: Int,
    modifier: Modifier = Modifier
){
    val description = if (value < 0 || max < 0) ""
                      else "${DecimalFormat("#.##").format(value)} / $max"
    Card(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
        )
        Text(
            text = description,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        LinearProgressIndicator(
            progress = { value / max },
            modifier = Modifier.fillMaxWidth()
        )
    }
}