package it.fra.myunical.today.composables

import it.fra.myunical.ui.rememberLinkOpener
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Grading
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.domain.model.degreeexplorer.DegreeRegulations
import it.fra.myunical.domain.model.degreeexplorer.Regulation
import it.fra.myunical.today.models.CANTEEN_LINK_ID
import it.fra.myunical.today.models.CampusLinks
import it.fra.myunical.today.models.ProfileUiState
import it.fra.myunical.today.presentation.StudentAvatar
import it.fra.myunical.today.presentation.TodayScreenViewModel
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.GroupedListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Where a profile menu entry leads. */
sealed interface ProfileDestination {
    data class Web(val url: String) : ProfileDestination
    data object Taxes : ProfileDestination
    data object Directory : ProfileDestination
    data object Canteen : ProfileDestination
}

private const val StudentSupportUrl = "https://www.unical.it/didattica/diritto-allo-studio/"
private const val WEB_OPEN_DELAY_MS = 120L
private const val SCREEN_TRANSITION_MS = 700L

private data class ProfileEntry(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val destination: ProfileDestination
)

private val StudentEntries = listOf(
    ProfileEntry("Libretto online", "Esami, voti e crediti su Esse3", Icons.Rounded.MenuBook, ProfileDestination.Web(Esse3Links.LIBRETTO)),
    ProfileEntry("Appelli e prenotazioni", "Iscriviti agli esami e gestisci le prenotazioni", Icons.AutoMirrored.Rounded.EventNote, ProfileDestination.Web(Esse3Links.EXAM_BOOKINGS)),
    ProfileEntry("Bacheca esiti", "Accetta o rifiuta i voti pubblicati", Icons.Rounded.Grading, ProfileDestination.Web(Esse3Links.EXAM_RESULTS)),
    ProfileEntry("Piano di studi", "Consulta e compila il piano", Icons.AutoMirrored.Rounded.ListAlt, ProfileDestination.Web(Esse3Links.STUDY_PLAN)),
    ProfileEntry("Tasse e pagamenti", "Addebiti, scadenze e pagoPA", Icons.Rounded.Payments, ProfileDestination.Taxes)
)

private val ServiceEntries = listOf(
    ProfileEntry("Documenti e certificati", "Certificati e autocertificazioni su Esse3", Icons.Rounded.Description, ProfileDestination.Web(Esse3Links.CERTIFICATES)),
    ProfileEntry("Diritto allo studio", "Borse di studio, alloggi e altri benefici", Icons.Rounded.VolunteerActivism, ProfileDestination.Web(StudentSupportUrl)),
    ProfileEntry("Rubrica di ateneo", "Contatti di docenti e personale", Icons.Rounded.Contacts, ProfileDestination.Directory),
    ProfileEntry("Portale Esse3", "Tutti i servizi per gli studenti", Icons.Rounded.School, ProfileDestination.Web(Esse3Links.HOME))
)

private val CampusEntries = CampusLinks.map {
    ProfileEntry(it.title, it.description, it.icon, if (it.id == CANTEEN_LINK_ID) ProfileDestination.Canteen else ProfileDestination.Web(it.destinationUrl))
}

private fun academicYearLabel(year: Int) = "a.a. $year/${"%02d".format((year + 1) % 100)}"

/** Links to the PDF regulations of the student's degree course, as many as are published. */
private fun regulationEntries(regulations: DegreeRegulations?): List<ProfileEntry> {
    if (regulations == null) return emptyList()
    val cohort = regulations.cohort
    val latest = regulations.latest
    fun entry(title: String, description: String, regulation: Regulation) =
        ProfileEntry(title, description, Icons.Rounded.Gavel, ProfileDestination.Web(regulation.url))
    return when {
        regulations.isSingle && cohort != null ->
            listOf(entry("Regolamento del corso", academicYearLabel(cohort.academicYear), cohort))
        else -> listOfNotNull(
            latest?.let { entry("Regolamento in vigore", "Ultima versione, ${academicYearLabel(it.academicYear)}", it) },
            cohort?.let { entry("Regolamento della tua immatricolazione", "Coorte ${academicYearLabel(it.academicYear)}", it) }
        )
    }
}

/** Profile menu: the stateful entry point, hosted above the navigation bar by the main screen. */
@Composable
fun ProfileMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenCanteen: () -> Unit,
    viewModel: TodayScreenViewModel = koinViewModel()
) {
    val openLink = rememberLinkOpener()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val regulations by viewModel.regulations.collectAsStateWithLifecycle()
    ProfileSheet(
        visible = visible,
        profile = profile,
        regulations = regulations,
        onDismiss = onDismiss,
        onSelectCareer = viewModel::selectCareer,
        onNavigate = { destination ->
            when (destination) {
                is ProfileDestination.Web -> openLink(destination.url)
                ProfileDestination.Taxes -> onOpenTaxes()
                ProfileDestination.Directory -> onOpenDirectory()
                ProfileDestination.Canteen -> onOpenCanteen()
            }
        },
        onLogout = viewModel::logout
    )
}

@Composable
fun ProfileSheet(
    visible: Boolean,
    profile: ProfileUiState?,
    regulations: DegreeRegulations?,
    onDismiss: () -> Unit,
    onSelectCareer: (Int) -> Unit,
    onNavigate: (ProfileDestination) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    fun open(destination: ProfileDestination) {
        scope.launch {
            if (destination is ProfileDestination.Web) {
                // A Custom Tab covers the app: let the sheet start moving down first
                onDismiss()
                delay(WEB_OPEN_DELAY_MS)
                onNavigate(destination)
            } else {
                // The sheet belongs to Home: the new screen slides over both in a single
                // movement, then the sheet is closed out of sight
                onNavigate(destination)
                delay(SCREEN_TRANSITION_MS)
                onDismiss()
            }
        }
    }

    OverlaySheet(visible = visible, onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            if (profile != null) ProfileHeader(profile, onSelectCareer)

            EntryGroup("Studente", StudentEntries + regulationEntries(regulations), ::open)
            EntryGroup("Servizi", ServiceEntries, ::open)
            EntryGroup("Campus", CampusEntries, ::open)

            GroupHeader("Account")
            GroupedListItem(
                title = "Esci",
                supporting = "Rimuove le credenziali salvate su questo dispositivo",
                icon = Icons.AutoMirrored.Rounded.Logout,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconColor = MaterialTheme.colorScheme.onErrorContainer,
                titleColor = MaterialTheme.colorScheme.error,
                index = 0,
                count = 1,
                onClick = { confirmLogout = true }
            )
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
            title = { Text("Vuoi uscire?") },
            text = { Text("Per usare di nuovo l'app dovrai accedere con le credenziali di Esse3.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onDismiss()
                    onLogout()
                }) { Text("Esci") }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun ProfileHeader(profile: ProfileUiState, onSelectCareer: (Int) -> Unit) {
    var showCareers by rememberSaveable { mutableStateOf(false) }
    val canSwitch = profile.careers.size > 1
    val chevronRotation by animateFloatAsState(if (showCareers) 180f else 0f, label = "careerChevron")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        StudentAvatar(avatarUrl = profile.avatarUrl, size = 64.dp)
        Column(Modifier.weight(1f)) {
            Text(profile.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Matricola ${profile.matricola}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Surface(
        onClick = { if (canSwitch) showCareers = !showCareers },
        enabled = canSwitch,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column(Modifier.weight(1f)) {
                Text(
                    profile.courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (canSwitch) {
                    Text(
                        "Cambia carriera",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            if (canSwitch) {
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = if (showCareers) "Nascondi carriere" else "Mostra carriere",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = showCareers,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(Modifier.padding(top = 8.dp)) {
            profile.careers.forEachIndexed { index, career ->
                GroupedListItem(
                    title = career.name,
                    supporting = "${career.status} · Matr. ${career.matricola}",
                    index = index,
                    count = profile.careers.size,
                    onClick = {
                        onSelectCareer(career.matId)
                        showCareers = false
                    },
                    trailing = { RadioButton(selected = career.isSelected, onClick = null) }
                )
            }
        }
    }
}

@Composable
private fun EntryGroup(
    title: String,
    entries: List<ProfileEntry>,
    onClick: (ProfileDestination) -> Unit
) {
    GroupHeader(title)
    entries.forEachIndexed { index, entry ->
        GroupedListItem(
            title = entry.title,
            supporting = entry.description,
            icon = entry.icon,
            index = index,
            count = entries.size,
            onClick = { onClick(entry.destination) },
            trailing = {
                Icon(
                    if (entry.destination is ProfileDestination.Web) Icons.AutoMirrored.Rounded.OpenInNew
                    else Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}
