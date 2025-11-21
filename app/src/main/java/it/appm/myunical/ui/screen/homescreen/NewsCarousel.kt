package it.appm.myunical.ui.screen.homescreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.appm.myunical.R
import it.appm.myunical.data.DataRepository
import it.appm.myunical.model.News

@Composable
fun NewsCarousel(news: List<News>?){
    val scrollState = rememberScrollState()
    Column {
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.news),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp).align(Alignment.CenterStart),
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {},
                modifier = Modifier.padding(16.dp).align(Alignment.CenterEnd),
                content = {
                    Text(
                        text = stringResource(R.string.show_more),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
        Spacer(Modifier)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.size(8.dp))
            if (news.isNullOrEmpty()){
                repeat(5){
                    NewsPlaceholder()
                }
            } else {
                news.forEach {
                    NewsItem(it.title, it.image, it.url)
                }
            }
        }
    }
}

@Composable
private fun NewsItem(title: String, path: String, url: String){
    val ctx = LocalContext.current
    Card(
        Modifier.size(192.dp, 200.dp).clickable {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    ) {
        AsyncImage(
            model = path,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray.copy(alpha = 0.25f))
                .height(120.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NewsPlaceholder(modifier: Modifier = Modifier){
    Box(
        Modifier
            .size(192.dp, 200.dp)
            .background(Color.Black.copy(0.25f), CardDefaults.shape)
            .then(modifier)
    )
}