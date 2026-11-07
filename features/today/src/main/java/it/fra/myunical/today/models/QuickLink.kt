package it.fra.myunical.today.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.theme.Accent

data class QuickLink(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val destinationUrl: String,
    /** Longer explanation shown in the profile menu. */
    val description: String = "",
    /** Color family of the chip, so each service is recognizable at a glance. */
    val accent: Accent = Accent.NEUTRAL
)

private val Esse3Link = QuickLink("esse3", "Esse3", Icons.Rounded.School, Esse3Links.HOME, "Portale dei servizi per gli studenti", Accent.PRIMARY)

private val DocumentsLink = QuickLink("documents", "Documenti", Icons.Rounded.Description, Esse3Links.CERTIFICATES, "Certificati e autocertificazioni su Esse3", Accent.INFO)

/** Campus services opened in a Custom Tab; every URL was checked to be reachable. */
val CampusLinks = listOf(
    QuickLink("mail", "Webmail", Icons.Rounded.Mail, "https://outlook.com/studenti.unical.it", "Posta istituzionale @studenti.unical.it", Accent.SECONDARY),
    QuickLink(CANTEEN_LINK_ID, "Mensa", Icons.Rounded.Restaurant, "https://www.unical.it/campus/vivere-il-campus/servizio-mensa/", "Menù del giorno, pranzo e cena", Accent.INFO),
    QuickLink("library", "Biblioteche", Icons.Rounded.LocalLibrary, "https://www.unical.it/campus/vivere-il-campus/biblioteche/", "Orari, cataloghi e prestiti", Accent.TERTIARY),
    QuickLink("map", "Mappa campus", Icons.Rounded.Map, "https://www.unical.it/campus/visita-il-campus/mappa/", "Cubi, aule e servizi del campus", Accent.SUCCESS),
    QuickLink("site", "Sito UniCal", Icons.Rounded.Public, "https://www.unical.it/", "Il sito dell'Università della Calabria", Accent.WARNING)
)

/** The canteen link opens the menus in the app instead of the site (profile menu only). */
const val CANTEEN_LINK_ID = "canteen"

/** Shortcuts in the Home chips row. */
val DefaultQuickLinks = listOf(Esse3Link, DocumentsLink) + CampusLinks.filter { it.id != "site" && it.id != CANTEEN_LINK_ID }
