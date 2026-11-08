package it.appm.myunical.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import it.appm.myunical.ui.navigation.MainTab
import it.appm.myunical.ui.theme.AppTheme
import it.fra.myunical.canteen.mapper.toDayOption
import it.fra.myunical.canteen.models.CanteenUiState
import it.fra.myunical.canteen.models.CourseUiModel
import it.fra.myunical.canteen.models.DishUiModel
import it.fra.myunical.canteen.presentation.CanteenScreen
import it.fra.myunical.career.models.CareerOption
import it.fra.myunical.career.models.CareerUiState
import it.fra.myunical.career.models.ExamGroup
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.OverviewUiModel
import it.fra.myunical.career.presentation.CareerScreen
import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.news.models.NewsItemUiModel
import it.fra.myunical.news.models.NewsListUiState
import it.fra.myunical.news.models.NewsScreenState
import it.fra.myunical.news.models.NewsTab
import it.fra.myunical.news.models.NewsTabUi
import it.fra.myunical.news.presentation.list.NewsListScreen
import it.fra.myunical.schedule.models.LessonUiModel
import it.fra.myunical.schedule.models.ProviderFilter
import it.fra.myunical.schedule.models.ScheduleDay
import it.fra.myunical.schedule.models.ScheduleUiState
import it.fra.myunical.schedule.presentation.schedule.ScheduleScreen
import it.fra.myunical.today.models.CanteenCardState
import it.fra.myunical.today.models.CareerSnapshotState
import it.fra.myunical.today.models.CommitmentType
import it.fra.myunical.today.models.DefaultQuickLinks
import it.fra.myunical.today.models.DepartmentNotices
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.today.models.StudentHeaderInfo
import it.fra.myunical.today.models.TuitionState
import it.fra.myunical.today.models.UpcomingCommitment
import it.fra.myunical.today.models.UpcomingCommitmentState
import it.fra.myunical.today.presentation.TodayScreen
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/*
 * Screenshots of the README (docs/screenshots), rendered from these previews with
 * `./gradlew :app:updateDebugScreenshotTest`. Every name, number and lesson is made up:
 * the README is public, so nothing here may come from a real account.
 */

private const val PHONE = "spec:width=411dp,height=891dp"
private val Today: LocalDate = LocalDate.of(2026, 11, 9)

@Composable
private fun Phone(content: @Composable () -> Unit) {
    AppTheme(darkTheme = true, content = content)
}

/** The main screen chrome: a tab content above the bottom navigation. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WithNavigation(selected: MainTab, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = {
            ShortNavigationBar {
                MainTab.entries.forEach { tab ->
                    ShortNavigationBarItem(
                        selected = tab == selected,
                        onClick = {},
                        icon = { Icon(if (tab == selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = null) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) { content() }
    }
}

@PreviewTest
@Preview(name = "home", device = PHONE)
@Composable
fun HomeScreenshot() = Phone {
    WithNavigation(MainTab.HOME) {
        TodayScreen(
            student = StudentHeaderInfo(firstName = "Giulia", matricola = "204512"),
            upcomingCommitment = UpcomingCommitmentState.Available(
                UpcomingCommitment(
                    title = "Ingegneria del Software",
                    type = CommitmentType.LESSON,
                    timeSlot = "09:30 – 11:30",
                    room = "Aula MT4, Cubo 21B",
                    relativeTime = "Tra 35 min"
                )
            ),
            careerSnapshot = CareerSnapshotState.Available(114, 180, 0.63f, "27,68", 2),
            tuitionState = TuitionState.Due(amount = "148,50 €", dueLabel = "scade il 30/11", pendingCount = 1, isOverdue = false),
            canteen = CanteenCardState.Available("Oggi a pranzo", listOf("Pasta al pesto", "Risotto alla zucca"), 15),
            quickLinks = DefaultQuickLinks,
            notices = emptyList(),
            departmentNotices = DepartmentNotices(
                "DIMES",
                listOf(
                    NoticeUiModel("1", "Calendario degli esami della sessione invernale", "Didattica", "Oggi", ""),
                    NoticeUiModel("2", "Seminario sulle reti di nuova generazione", "Eventi", "Ieri", ""),
                    NoticeUiModel("3", "Aule studio aperte anche il sabato", "Avvisi", "5 nov", "")
                )
            )
        )
    }
}

@PreviewTest
@Preview(name = "career", device = PHONE)
@Composable
fun CareerScreenshot() = Phone {
    WithNavigation(MainTab.CAREER) {
        CareerScreen(
            state = CareerUiState(
                isLoading = false,
                careerName = "Ingegneria Informatica",
                careers = listOf(CareerOption(1, "Ingegneria Informatica", "Attiva", "204512", true)),
                overview = OverviewUiModel(114, 180, 0.63f, "27,68", "27,76", "101,51", "15 di 23 esami superati"),
                examGroups = listOf(
                    ExamGroup(
                        "1° anno",
                        listOf(
                            ExamRecord(1, "Analisi Matematica I", 9, "Scritto e orale", "30L", true, true, "15/02/2025", 0),
                            ExamRecord(2, "Fisica Generale", 9, "Scritto", "27", false, true, "22/06/2025", 0),
                            ExamRecord(3, "Fondamenti di Informatica", 12, "Scritto e orale", "29", false, true, "10/07/2025", 0),
                            ExamRecord(4, "Lingua Inglese", 3, null, "Idoneo", false, true, "10/01/2025", 0)
                        )
                    ),
                    ExamGroup(
                        "2° anno",
                        listOf(
                            ExamRecord(5, "Basi di Dati", 9, "Scritto e orale", null, false, false, null, 2),
                            ExamRecord(6, "Ingegneria del Software", 9, "Progetto e orale", null, false, false, null, 1)
                        )
                    )
                )
            ),
            onIntent = {},
            onOpenPortal = {},
            onOpenCourse = {}
        )
    }
}

@PreviewTest
@Preview(name = "schedule", device = PHONE)
@Composable
fun ScheduleScreenshot() = Phone {
    fun lesson(key: String, code: String, title: String, day: LocalDate, from: Int, to: Int, room: String, teacher: String) =
        LessonUiModel(key, null, code, title, day, LocalTime.of(from, 30), LocalTime.of(to, 30), room, teacher)
    val tomorrow = Today.plusDays(1)
    WithNavigation(MainTab.SCHEDULE) {
        ScheduleScreen(
            state = ScheduleUiState(
                month = YearMonth.from(Today),
                today = Today,
                providers = listOf(ProviderFilter("p", "Ingegneria Informatica", true)),
                days = listOf(
                    ScheduleDay(
                        Today,
                        listOf(
                            lesson("1", "A1", "Ingegneria del Software", Today, 9, 11, "MT4, Cubo 21B", "Rossi Mario"),
                            lesson("2", "A2", "Basi di Dati", Today, 11, 13, "MT6, Cubo 22B", "Bianchi Laura"),
                            lesson("3", "A3", "Reti di Calcolatori", Today, 14, 16, "Lab 1, Cubo 41C", "Verdi Paolo")
                        )
                    ),
                    ScheduleDay(
                        tomorrow,
                        listOf(lesson("4", "A2", "Basi di Dati", tomorrow, 8, 10, "MT6, Cubo 22B", "Bianchi Laura"))
                    )
                )
            ),
            onIntent = {},
            onManageProviders = {},
            onAddProvider = {}
        )
    }
}

@PreviewTest
@Preview(name = "news", device = PHONE)
@Composable
fun NewsScreenshot() = Phone {
    val items = listOf(
        NewsItemUiModel("1", "Calendario degli esami della sessione invernale", "Pubblicate le date degli appelli di gennaio e febbraio per tutti i corsi di laurea.", "Didattica", "Oggi", null),
        NewsItemUiModel("2", "Aule studio aperte anche il sabato", "Da novembre le aule studio dei cubi 30 e 31 restano aperte fino alle 20.", "Avvisi", "Ieri", null),
        NewsItemUiModel("3", "Bando per le collaborazioni studentesche", "Le domande vanno presentate entro il 30 novembre tramite il portale.", "Bandi", "6 nov", null),
        NewsItemUiModel("4", "Sospensione delle lezioni per la festa patronale", "Le attività didattiche sono sospese per l'intera giornata.", "Avvisi", "5 nov", null),
        NewsItemUiModel("5", "Nuovi orari della biblioteca", "La biblioteca centrale amplia l'orario di apertura serale.", "Servizi", "3 nov", null)
    )
    NewsListScreen(
        screen = NewsScreenState(
            tabs = listOf(NewsTabUi(NewsTab.NEWS, "Notizie"), NewsTabUi(NewsTab.NOTICES, "Avvisi"), NewsTabUi(NewsTab.DEPARTMENT, "DIMES")),
            selected = NewsTab.NOTICES,
            lists = mapOf(NewsTab.NOTICES to NewsListUiState(items = items, isLoading = false))
        ),
        onBack = {},
        onOpenNews = {},
        onSelectTab = {},
        onLoadMore = {},
        onRefresh = {}
    )
}

@PreviewTest
@Preview(name = "canteen", device = PHONE)
@Composable
fun CanteenScreenshot() = Phone {
    CanteenScreen(
        state = CanteenUiState(
            isLoading = false,
            days = (0L..6L).map { Today.plusDays(it).toDayOption(Today) },
            selectedDate = Today,
            coveredUntil = "15 novembre",
            courses = listOf(
                CourseUiModel(
                    CourseType.FIRST, "Primi piatti", listOf(
                        DishUiModel("Pasta al pesto", DishLine.CLASSIC, emptyList(), false),
                        DishUiModel("Penne con salmone", DishLine.CLASSIC, emptyList(), true),
                        DishUiModel("Pennette con broccoletti", DishLine.VEGETARIAN, listOf(DietTag.VEGETARIAN), false),
                        DishUiModel("Minestra di legumi", DishLine.SOUP, listOf(DietTag.VEGAN), false)
                    )
                ),
                CourseUiModel(
                    CourseType.SECOND, "Secondi piatti", listOf(
                        DishUiModel("Petto di pollo alla griglia", DishLine.CLASSIC, emptyList(), false),
                        DishUiModel("Frittata di verdure", DishLine.VEGETARIAN, listOf(DietTag.VEGETARIAN), false),
                        DishUiModel("Insalata caprese", DishLine.COLD, listOf(DietTag.VEGETARIAN), false)
                    )
                )
            )
        ),
        onIntent = {},
        onBack = {},
        onOpenPage = {}
    )
}
