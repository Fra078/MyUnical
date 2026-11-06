package it.fra.myunical.today.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import it.fra.myunical.ui.Esse3Links

data class QuickLink(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val destinationUrl: String,
    /** Longer explanation shown in the profile menu. */
    val description: String = ""
)

private val Esse3Link = QuickLink("esse3", "Esse3", Icons.Rounded.School, Esse3Links.HOME, "Portale dei servizi per gli studenti")

/** Campus services opened in a Custom Tab; every URL was checked to be reachable. */
val CampusLinks = listOf(
    QuickLink("mail", "Webmail", Icons.Rounded.Mail, "https://outlook.com/studenti.unical.it", "Posta istituzionale @studenti.unical.it"),
    QuickLink("canteen", "Mensa", Icons.Rounded.Restaurant, "https://www.unical.it/campus/vivere-il-campus/servizio-mensa/", "Menù, orari e punti ristoro"),
    QuickLink("library", "Biblioteche", Icons.Rounded.LocalLibrary, "https://www.unical.it/campus/vivere-il-campus/biblioteche/", "Orari, cataloghi e prestiti"),
    QuickLink("map", "Mappa campus", Icons.Rounded.Map, "https://www.unical.it/campus/visita-il-campus/mappa/", "Cubi, aule e servizi del campus"),
    QuickLink("site", "Sito UniCal", Icons.Rounded.Public, "https://www.unical.it/", "Il sito dell'Università della Calabria")
)

/** Shortcuts in the Home chips row. */
val DefaultQuickLinks = listOf(Esse3Link) + CampusLinks.filter { it.id != "site" }
