package it.fra.myunical.news.presentation.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.fra.myunical.news.models.NewsDetailUiModel
import it.fra.myunical.news.models.NewsDetailUiState
import it.fra.myunical.ui.animation.newsImageKey
import it.fra.myunical.ui.animation.sharedImage
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.openInCustomTab
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NewsDetailScreen(
    newsId: String,
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = koinViewModel(key = "news_$newsId") { parametersOf(newsId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NewsDetailScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewsDetailScreen(
    state: NewsDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val news = (state as? NewsDetailUiState.Loaded)?.news

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {},
                subtitle = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (news != null) {
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_SUBJECT, news.title)
                                .putExtra(Intent.EXTRA_TEXT, "${news.title}\n${news.link}")
                            context.startActivity(Intent.createChooser(send, "Condividi notizia"))
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Condividi")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                NewsDetailUiState.Loading -> LoadingIndicator(Modifier.size(64.dp).align(Alignment.Center))
                NewsDetailUiState.NotFound -> EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "Notizia non disponibile",
                    description = "Torna all'elenco delle notizie e riaprila.",
                    polygon = MaterialShapes.Cookie6Sided
                )
                is NewsDetailUiState.Loaded -> NewsArticle(state.news, onOpenSite = { context.openInCustomTab(state.news.link) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewsArticle(news: NewsDetailUiModel, onOpenSite: () -> Unit) {
    val linkColor = MaterialTheme.colorScheme.primary
    val body = remember(news.contentHtml, linkColor) {
        AnnotatedString.fromHtml(
            news.contentHtml,
            linkStyles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (news.imageUrl != null) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .sharedImage(newsImageKey(news.id), cornerRadius = 28.dp)
            )
        }
        Text(
            listOf(news.category, news.dateLabel.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(news.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (news.summary.isNotBlank()) {
            Text(news.summary, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge)
        if (news.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                news.tags.forEach { tag -> SuggestionChip(onClick = {}, label = { Text("#$tag") }) }
            }
        }
        Button(onClick = onOpenSite, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Apri sul sito UniCal")
        }
    }
}
