package it.appm.myunical.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Main : AppRoute

    @Serializable
    data object ScheduleProviders : AppRoute

    /** [providerId] is `null` when a new schedule is being created. */
    @Serializable
    data class ScheduleProviderEditor(val providerId: String? = null) : AppRoute

    @Serializable
    data object Taxes : AppRoute

    @Serializable
    data class News(val openDepartment: Boolean = false) : AppRoute

    @Serializable
    data class NewsDetail(val newsId: String) : AppRoute

    @Serializable
    data object Directory : AppRoute

    @Serializable
    data object Canteen : AppRoute

    @Serializable
    data class ContactDetail(val contactId: String) : AppRoute

    /** An Esse3 page shown signed in; [url] is one of `Esse3Links`. */
    @Serializable
    data class Esse3Web(val url: String) : AppRoute

    /** [courseRowId] is the libretto row (adsceId) of the current career. */
    @Serializable
    data class CourseDetail(val courseRowId: Long) : AppRoute
}

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CAREER("Carriera", Icons.Filled.School, Icons.Outlined.School),
    SCHEDULE("Orario", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
}
