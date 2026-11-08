package it.appm.myunical.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import it.appm.myunical.ui.screen.main.MainScreen
import it.fra.myunical.canteen.presentation.CanteenScreen
import it.fra.myunical.career.presentation.detail.CourseDetailScreen
import it.fra.myunical.directory.presentation.detail.ContactDetailScreen
import it.fra.myunical.directory.presentation.search.DirectoryScreen
import it.fra.myunical.news.presentation.detail.NewsDetailScreen
import it.fra.myunical.news.presentation.list.NewsListScreen
import it.fra.myunical.schedule.presentation.editor.ScheduleProviderEditorScreen
import it.fra.myunical.schedule.presentation.providers.ScheduleProvidersScreen
import it.fra.myunical.taxes.presentation.TaxesScreen
import it.fra.myunical.ui.animation.LocalNavVisibilityScope
import it.fra.myunical.ui.animation.LocalSharedCornerRegistry
import it.fra.myunical.ui.animation.LocalSharedTransitionScope
import it.fra.myunical.ui.animation.SharedCornerRegistry

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AppRoute.Main)

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    val cornerRegistry = remember { SharedCornerRegistry() }

    // Whatever the screens leave uncovered while they animate shows the theme, not the window
    SharedTransitionLayout(modifier.background(MaterialTheme.colorScheme.background)) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalSharedCornerRegistry provides cornerRegistry
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = ::pop,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                sharedTransitionScope = this,
                transitionSpec = slideOverForward(),
                popTransitionSpec = slideOverBackward(),
                predictivePopTransitionSpec = predictiveBack(),
                entryProvider = entryProvider<NavKey> {
                    entry<AppRoute.Main> {
                        WithNavScope {
                            MainScreen(
                                onManageSchedules = { backStack.add(AppRoute.ScheduleProviders) },
                                onAddSchedule = { backStack.add(AppRoute.ScheduleProviderEditor()) },
                                onOpenTaxes = { backStack.add(AppRoute.Taxes) },
                                onOpenNews = { backStack.add(AppRoute.News()) },
                                onOpenDepartmentNews = { backStack.add(AppRoute.News(openDepartment = true)) },
                                onOpenNewsDetail = { backStack.add(AppRoute.NewsDetail(it)) },
                                onOpenDirectory = { backStack.add(AppRoute.Directory) },
                                onOpenCourse = { backStack.add(AppRoute.CourseDetail(it)) },
                                onOpenCanteen = { backStack.add(AppRoute.Canteen) }
                            )
                        }
                    }
                    entry<AppRoute.ScheduleProviders> {
                        ScheduleProvidersScreen(
                            onBack = ::pop,
                            onAddProvider = { backStack.add(AppRoute.ScheduleProviderEditor()) },
                            onEditProvider = { backStack.add(AppRoute.ScheduleProviderEditor(it)) }
                        )
                    }
                    entry<AppRoute.News> { route ->
                        WithNavScope {
                            NewsListScreen(
                                onBack = ::pop,
                                openDepartment = route.openDepartment,
                                onOpenNews = { backStack.add(AppRoute.NewsDetail(it)) }
                            )
                        }
                    }
                    entry<AppRoute.NewsDetail> { route ->
                        WithNavScope {
                            NewsDetailScreen(newsId = route.newsId, onBack = ::pop)
                        }
                    }
                    entry<AppRoute.Taxes> {
                        TaxesScreen(onBack = ::pop)
                    }
                    entry<AppRoute.Canteen> {
                        CanteenScreen(onBack = ::pop)
                    }
                    entry<AppRoute.Directory> {
                        DirectoryScreen(
                            onBack = ::pop,
                            onOpenContact = { backStack.add(AppRoute.ContactDetail(it)) }
                        )
                    }
                    entry<AppRoute.CourseDetail> { route ->
                        CourseDetailScreen(
                            courseRowId = route.courseRowId,
                            onBack = ::pop,
                            onOpenContact = { backStack.add(AppRoute.ContactDetail(it)) }
                        )
                    }
                    entry<AppRoute.ContactDetail> { route ->
                        ContactDetailScreen(contactId = route.contactId, onBack = ::pop)
                    }
                    entry<AppRoute.ScheduleProviderEditor> { route ->
                        ScheduleProviderEditorScreen(
                            providerId = route.providerId,
                            onClose = {
                                // Guard against a double close (save event + back press racing)
                                if (backStack.lastOrNull() == route) pop()
                            }
                        )
                    }
                }
            )
        }
    }
}

/** Exposes the entry's animation scope to feature modules, which don't depend on Navigation 3. */
@Composable
private fun WithNavScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavVisibilityScope provides LocalNavAnimatedContentScope.current, content = content)
}
