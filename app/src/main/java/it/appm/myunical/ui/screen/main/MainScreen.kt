package it.appm.myunical.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.appm.myunical.ui.navigation.MainTab
import it.fra.myunical.career.presentation.CareerScreen
import it.fra.myunical.schedule.presentation.settings.ScheduleProviderConfigurationScreen
import it.fra.myunical.today.presentation.TodayScreen

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Preserves tab history across configuration changes
    val tabBackStack = rememberSaveable(
        saver = listSaver(
            save = { stateList -> stateList.map { it.name } },
            restore = { savedList -> savedList.map { MainTab.valueOf(it) }.toMutableStateList() }
        )
    ) {
        mutableStateListOf(MainTab.HOME)
    }

    val currentTab = tabBackStack.last()

    // Navigates back through tab history before delegating to root backstack
    BackHandler(enabled = tabBackStack.size > 1) {
        tabBackStack.removeAt(tabBackStack.lastIndex)
    }

    fun selectTab(targetTab: MainTab) {
        if (targetTab != currentTab) {
            tabBackStack.remove(targetTab)
            tabBackStack.add(targetTab)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom),
        //floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            //FloatingNavigationBar(currentTab, ::selectTab)
            NavigationBar {
                    MainTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            alwaysShowLabel = false
                        )
                    }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TabContentAnimation",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) { activeTab ->
            when (activeTab) {
                MainTab.HOME -> TodayScreen()
                MainTab.SEARCH -> CareerScreen()
                MainTab.FAVORITES -> ScheduleProviderConfigurationScreen()
                MainTab.PROFILE -> {}
            }
        }
    }
}


@Composable
private fun FloatingNavigationBar(currentTab: MainTab, selectTab: (MainTab) -> Unit, modifier: Modifier = Modifier){
    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        content = {
            MainTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                IconButton(
                    onClick = { selectTab(tab) },
                    content = {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.title
                        )
                    }
                )
            }
        }
    )
}