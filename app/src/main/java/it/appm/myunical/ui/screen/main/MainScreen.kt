package it.appm.myunical.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import it.appm.myunical.ui.navigation.MainTab
import it.appm.myunical.ui.navigation.fadeThrough
import it.fra.myunical.career.presentation.CareerScreen
import it.fra.myunical.schedule.presentation.schedule.ScheduleScreen
import it.fra.myunical.today.composables.ProfileMenu
import it.fra.myunical.today.presentation.TodayScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    onManageSchedules: () -> Unit,
    onAddSchedule: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenDepartmentNews: () -> Unit,
    onOpenNewsDetail: (String) -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenCourse: (Long) -> Unit,
    onOpenCanteen: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Preserves tab history across configuration changes
    val tabBackStack = rememberSaveable(
        saver = listSaver(
            save = { stateList -> stateList.map { it.name } },
            restore = { savedList ->
                // Tabs may have been renamed between app versions
                savedList.mapNotNull { name -> MainTab.entries.firstOrNull { it.name == name } }
                    .ifEmpty { listOf(MainTab.HOME) }
                    .toMutableStateList()
            }
        )
    ) {
        mutableStateListOf(MainTab.HOME)
    }

    val currentTab = tabBackStack.last()
    // Tapping the selected Career tab again scrolls it back to the top
    var careerReselections by rememberSaveable { mutableIntStateOf(0) }

    // Navigates back through tab history before delegating to root backstack
    BackHandler(enabled = tabBackStack.size > 1) {
        tabBackStack.removeAt(tabBackStack.lastIndex)
    }

    fun selectTab(targetTab: MainTab) {
        if (targetTab != currentTab) {
            tabBackStack.remove(targetTab)
            tabBackStack.add(targetTab)
        } else if (targetTab == MainTab.CAREER) {
            careerReselections++
        }
    }

    // Not saved: coming back from a screen opened by the menu shows Home without it
    var showProfile by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom),
            bottomBar = {
                ShortNavigationBar {
                    MainTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        ShortNavigationBarItem(
                            selected = isSelected,
                            onClick = { selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeThrough() },
                label = "TabContentAnimation",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) { activeTab ->
                when (activeTab) {
                    MainTab.HOME -> TodayScreen(
                        onOpenSchedule = { selectTab(MainTab.SCHEDULE) },
                        onSetupSchedule = onAddSchedule,
                        onOpenTaxes = onOpenTaxes,
                        onOpenCareer = { selectTab(MainTab.CAREER) },
                        onOpenNews = onOpenNews,
                        onOpenNewsDetail = onOpenNewsDetail,
                        onOpenDepartmentNews = onOpenDepartmentNews,
                        onOpenProfile = { showProfile = true },
                        onOpenCanteen = onOpenCanteen
                    )
                    MainTab.CAREER -> CareerScreen(onOpenCourse = onOpenCourse, reselectSignal = careerReselections)
                    MainTab.SCHEDULE -> ScheduleScreen(
                        onManageProviders = onManageSchedules,
                        onAddProvider = onAddSchedule
                    )
                }
            }
        }

        // Above the navigation bar, and part of the screen so it moves with it during navigation
        ProfileMenu(
            visible = showProfile,
            onDismiss = { showProfile = false },
            onOpenTaxes = onOpenTaxes,
            onOpenDirectory = onOpenDirectory,
            onOpenCanteen = onOpenCanteen
        )
    }
}
