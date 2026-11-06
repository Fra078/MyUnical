@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package it.fra.myunical.today.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import it.fra.myunical.today.R
import it.fra.myunical.today.composables.NoticeCard
import it.fra.myunical.today.composables.QuickLinksSection
import it.fra.myunical.today.composables.TuitionStatusCard
import it.fra.myunical.today.composables.UpcomingCommitmentHeroCard
import it.fra.myunical.today.models.CommitmentType
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.today.models.QuickLink
import it.fra.myunical.today.models.StudentHeaderInfo
import it.fra.myunical.today.models.TuitionStatus
import it.fra.myunical.today.models.UpcomingCommitment
import org.koin.androidx.compose.koinViewModel


@Composable
fun TodayScreen(viewModel: TodayScreenViewModel = koinViewModel()){
    val studentInfo = viewModel.studentInfo.collectAsStateWithLifecycle().value

    val sampleCommitment = UpcomingCommitment(
        title = "Ingegneria del Software",
        type = CommitmentType.LESSON,
        timeSlot = "09:30 - 11:30",
        room = "Aula MT4, Cubo 21B",
        startsInMinutes = 35
    )

    val sampleTuition = TuitionStatus(
        isUpToDate = false,
        pendingAmount = 148.50,
        dueDate = "30/11/2026",
        installmentDescription = "II Rata Anno Accademico"
    )

    val sampleQuickLinks = listOf(
        QuickLink("1", "Portale Ateneo", R.drawable.language, "https://example.com"),
        QuickLink("2", "Certificati", R.drawable.description, "https://example.com"),
        QuickLink("3", "Webmail", R.drawable.mail, "https://example.com"),
        QuickLink("4", "Badge Digitale", R.drawable.badge, "https://example.com"),
        QuickLink("5", "Mensa", R.drawable.restaurant, "https://example.com")
    )

    val news = viewModel.newsList.collectAsStateWithLifecycle().value

    TodayScreen(
        student = studentInfo,
        upcomingCommitment = sampleCommitment,
        tuitionStatus = sampleTuition,
        quickLinks = sampleQuickLinks,
        notices = news,
        unreadNotificationsCount = 2
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    student: StudentHeaderInfo?,
    upcomingCommitment: UpcomingCommitment?,
    tuitionStatus: TuitionStatus,
    quickLinks: List<QuickLink>,
    notices: List<NoticeUiModel>,
    modifier: Modifier = Modifier,
    unreadNotificationsCount: Int = 0,
    onAvatarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onOpenPortalClick: () -> Unit = {},
    onQuickLinkClick: (QuickLink) -> Unit = {},
    onNoticeClick: (NoticeUiModel) -> Unit = {},
    onViewAllNoticesClick: () -> Unit = {},
    onTuitionClick: () -> Unit = {}
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = student?.let { "Ciao, ${it.firstName}"} ?: " ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student?.let { "Matr. ${it.matricola}"} ?: " ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onAvatarClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        StudentAvatar(
                            avatarUrl = student?.avatarUrl,
                            size = 40.dp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPortalClick) {
                        Icon(
                            painter = painterResource(R.drawable.language),
                            contentDescription = "Open university web portal"
                        )
                    }
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge { Text(text = "$unreadNotificationsCount") }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.notifications),
                                contentDescription = "Notifications"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            upcomingCommitment?.let { commitment ->
                item {
                    UpcomingCommitmentHeroCard(commitment = commitment)
                }
            }

            item {
                QuickLinksSection(
                    links = quickLinks,
                    onLinkClick = onQuickLinkClick
                )
            }

            item {
                TuitionStatusCard(
                    status = tuitionStatus,
                    onClick = onTuitionClick
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avvisi e Novità",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onViewAllNoticesClick) {
                        Text("Vedi tutti")
                    }
                }
            }

            if (notices.isEmpty())
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        LoadingIndicator(Modifier.size(64.dp))
                    }
                }

            items(
                items = notices,
                key = { it.id }
            ) { notice ->
                NoticeCard(
                    notice = notice,
                    onClick = { onNoticeClick(notice) }
                )
            }
        }
    }
}

@Composable
fun StudentAvatar(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNullOrBlank()) {
                AvatarPlaceholder(size = size)
            } else {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = "User profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                val state = painter.state.collectAsStateWithLifecycle().value
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    AvatarPlaceholder(size = size)
                }
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(size: Dp) {
    Icon(
        painter = painterResource(R.drawable.account_circle),
        contentDescription = "User profile placeholder",
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(size * 0.8f) // Il tuo ridimensionamento personalizzato
    )
}


@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    MaterialTheme {
        val sampleStudent = StudentHeaderInfo(
            firstName = "Vanessa",
            matricola = "223456"
        )

        val sampleCommitment = UpcomingCommitment(
            title = "Ingegneria del Software",
            type = CommitmentType.LESSON,
            timeSlot = "09:30 - 11:30",
            room = "Aula MT4, Cubo 21B",
            startsInMinutes = 35
        )

        val sampleTuition = TuitionStatus(
            isUpToDate = false,
            pendingAmount = 148.50,
            dueDate = "30/11/2026",
            installmentDescription = "II Rata Anno Accademico"
        )

        val sampleQuickLinks = listOf(
            QuickLink("1", "Portale Ateneo", R.drawable.language, "https://example.com"),
            QuickLink("2", "Certificati", R.drawable.description, "https://example.com"),
            QuickLink("3", "Webmail", R.drawable.mail, "https://example.com"),
            QuickLink("4", "Badge Digitale", R.drawable.badge, "https://example.com"),
            QuickLink("5", "Mensa", R.drawable.restaurant, "https://example.com")
        )

        val sampleNotices = listOf(
            NoticeUiModel(
                id = 1,
                title = "Inaugurazione Anno Accademico e lectio magistralis",
                category = "Eventi",
                date = "10 Ott",
                summary = "La cerimonia si terrà nell'Aula Magna alla presenza del corpo docente e delle autorità istituzionali.",
                imageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800"
            ),
            NoticeUiModel(
                id = 2,
                title = "Proroga termini presentazione piano di studi A.A. 2026/2027",
                category = "Didattica",
                date = "08 Ott",
                summary = "La scadenza per la compilazione online della modulistica è estesa al 30 novembre senza mora.",
                imageUrl = null
            )
        )

        TodayScreen(
            student = sampleStudent,
            upcomingCommitment = sampleCommitment,
            tuitionStatus = sampleTuition,
            quickLinks = sampleQuickLinks,
            notices = sampleNotices,
            unreadNotificationsCount = 2
        )
    }
}