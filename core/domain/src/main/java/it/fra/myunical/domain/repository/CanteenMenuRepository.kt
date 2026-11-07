package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.canteen.CanteenMenu

/** Canteen menus, read today from the file bundled with the app. Throws when they cannot be read. */
interface CanteenMenuRepository {
    suspend fun getMenu(): CanteenMenu
}
