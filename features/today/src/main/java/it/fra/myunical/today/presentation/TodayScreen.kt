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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
import it.fra.myunical.today.composables.CanteenCard
import it.fra.myunical.today.composables.CareerSnapshotCard
import it.fra.myunical.today.composables.DepartmentNoticesSection
import it.fra.myunical.today.composables.NewsCarousel
import it.fra.myunical.today.models.DepartmentNotices
import it.fra.myunical.today.composables.QuickLinksSection
import it.fra.myunical.today.composables.TuitionStatusCard
import it.fra.myunical.today.composables.UpcomingCommitmentHeroCard
import it.fra.myunical.today.models.CanteenCardState
import it.fra.myunical.today.models.CareerSnapshotState
import it.fra.myunical.today.models.CommitmentType
import it.fra.myunical.today.models.DefaultQuickLinks
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.today.models.QuickLink
import it.fra.myunical.today.models.StudentHeaderInfo
import it.fra.myunical.today.models.TuitionState
import it.fra.myunical.today.models.UpcomingCommitment
import it.fra.myunical.today.models.UpcomingCommitmentState
import it.fra.myunical.ui.rememberLinkOpener
import org.koin.androidx.compose.koinViewModel


@Composable
fun TodayScreen(
    onOpenSchedule: () -> Unit,
    onSetupSchedule: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenCareer: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenNewsDetail: (String) -> Unit,
    onOpenDepartmentNews: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCanteen: () -> Unit,
    viewModel: TodayScreenViewModel = koinViewModel()
){
    val openLink = rememberLinkOpener()
    val studentInfo = viewModel.studentInfo.collectAsStateWithLifecycle().value
    val upcomingCommitment = viewModel.upcomingCommitment.collectAsStateWithLifecycle().value
    val tuitionState = viewModel.tuitionState.collectAsStateWithLifecycle().value
    val careerSnapshot = viewModel.careerSnapshot.collectAsStateWithLifecycle().value
    val canteen = viewModel.canteen.collectAsStateWithLifecycle().value
    val news = viewModel.newsList.collectAsStateWithLifecycle().value
    val departmentNotices = viewModel.departmentNotices.collectAsStateWithLifecycle().value
    TodayScreen(
        student = studentInfo,
        upcomingCommitment = upcomingCommitment,
        careerSnapshot = careerSnapshot,
        tuitionState = tuitionState,
        canteen = canteen,
        quickLinks = DefaultQuickLinks,
        notices = news,
        departmentNotices = departmentNotices,
        onAvatarClick = onOpenProfile,
        onQuickLinkClick = { openLink(it.destinationUrl) },
        onNoticeClick = { onOpenNewsDetail(it.id) },
        onViewAllNoticesClick = onOpenNews,
        onViewAllDepartmentNoticesClick = onOpenDepartmentNews,
        onCareerClick = onOpenCareer,
        onTuitionClick = onOpenTaxes,
        onCanteenClick = onOpenCanteen,
        onOpenSchedule = onOpenSchedule,
        onSetupSchedule = onSetupSchedule
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    student: StudentHeaderInfo?,
    upcomingCommitment: UpcomingCommitmentState,
    careerSnapshot: CareerSnapshotState,
    tuitionState: TuitionState,
    canteen: CanteenCardState,
    quickLinks: List<QuickLink>,
    notices: List<NoticeUiModel>?,
    modifier: Modifier = Modifier,
    departmentNotices: DepartmentNotices? = null,
    onAvatarClick: () -> Unit = {},
    onQuickLinkClick: (QuickLink) -> Unit = {},
    onNoticeClick: (NoticeUiModel) -> Unit = {},
    onViewAllNoticesClick: () -> Unit = {},
    onViewAllDepartmentNoticesClick: () -> Unit = {},
    onCareerClick: () -> Unit = {},
    onTuitionClick: () -> Unit = {},
    onCanteenClick: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    onSetupSchedule: () -> Unit = {}
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
                            size = 40.dp,
                            contentDescription = "Apri il menù del profilo"
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "upcoming_commitment") {
                UpcomingCommitmentHeroCard(
                    state = upcomingCommitment,
                    onOpenSchedule = onOpenSchedule,
                    onSetupSchedule = onSetupSchedule
                )
            }

            item(key = "quick_links") {
                QuickLinksSection(
                    links = quickLinks,
                    onLinkClick = onQuickLinkClick
                )
            }

            item(key = "career") {
                CareerSnapshotCard(state = careerSnapshot, onClick = onCareerClick)
            }

            item(key = "tuition") {
                TuitionStatusCard(
                    state = tuitionState,
                    onClick = onTuitionClick
                )
            }

            item(key = "canteen") {
                CanteenCard(state = canteen, onClick = onCanteenClick)
            }

            item(key = "news_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notizie",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onViewAllNoticesClick) {
                        Text("Vedi tutte")
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(18.dp)
                        )
                    }
                }
            }

            item(key = "news") {
                when {
                    notices == null -> Box(
                        Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) { LoadingIndicator(Modifier.size(64.dp)) }
                    notices.isEmpty() -> Text(
                        "Notizie non disponibili al momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> NewsCarousel(notices = notices, onNoticeClick = onNoticeClick)
                }
            }

            if (departmentNotices != null) {
                item(key = "department_notices") {
                    DepartmentNoticesSection(
                        notices = departmentNotices,
                        onNoticeClick = onNoticeClick,
                        onViewAllClick = onViewAllDepartmentNoticesClick
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAvatar(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Foto profilo"
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
                    contentDescription = contentDescription,
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
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(size * 0.8f)
    )
}


@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    MaterialTheme {
        TodayScreen(
            student = StudentHeaderInfo(firstName = "Vanessa", matricola = "223456"),
            upcomingCommitment = UpcomingCommitmentState.Available(
                UpcomingCommitment(
                    title = "Ingegneria del Software",
                    type = CommitmentType.LESSON,
                    timeSlot = "09:30 - 11:30",
                    room = "Aula MT4, Cubo 21B",
                    relativeTime = "Tra 35 min"
                )
            ),
            careerSnapshot = CareerSnapshotState.Available(114, 180, 0.63f, "27,68", 2),
            tuitionState = TuitionState.Due(amount = "148,50 €", dueLabel = "scade il 30/11", pendingCount = 1, isOverdue = false),
            canteen = CanteenCardState.Available("Oggi a pranzo", listOf("Pasta al pesto", "Riso e zucca"), 15),
            quickLinks = DefaultQuickLinks,
            notices = listOf(
                NoticeUiModel("1", "Inaugurazione Anno Accademico e lectio magistralis", "Eventi", "Oggi", "", null),
                NoticeUiModel("2", "Proroga termini presentazione piano di studi", "Didattica", "Ieri", "", null)
            )
        )
    }
}
