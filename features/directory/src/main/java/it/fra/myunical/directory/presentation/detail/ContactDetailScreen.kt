package it.fra.myunical.directory.presentation.detail

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.directory.models.ContactDetailUiState
import it.fra.myunical.directory.models.initials
import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactRole
import it.fra.myunical.domain.model.directory.TeacherProfile
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.GroupedListItem
import it.fra.myunical.ui.components.openInCustomTab
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ContactDetailScreen(
    contactId: String,
    onBack: () -> Unit,
    viewModel: ContactDetailViewModel = koinViewModel(key = "contact_$contactId") { parametersOf(contactId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContactDetailScreen(state = state, onBack = onBack, onRetry = viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactDetailScreen(
    state: ContactDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                ContactDetailUiState.Loading -> LoadingIndicator(Modifier.size(64.dp).align(Alignment.Center))
                ContactDetailUiState.Error -> EmptyState(
                    icon = Icons.Rounded.PersonOff,
                    title = "Contatto non disponibile",
                    description = "Controlla la connessione e riprova.",
                    polygon = MaterialShapes.SoftBurst,
                    action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } }
                )
                is ContactDetailUiState.Loaded -> ContactDetail(state, snackbarHostState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContactDetail(state: ContactDetailUiState.Loaded, snackbarHostState: SnackbarHostState) {
    val contact = state.contact
    val teacher = state.teacher
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val copy: (String) -> Unit = { value ->
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(contact.name, value)))
            snackbarHostState.showSnackbar("Copiato negli appunti")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContactPhoto(photoUrl = teacher?.photoUrl, initials = contact.initials)
            Text(
                contact.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            contact.roles.firstOrNull()?.let {
                Text(
                    it.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            teacher?.scientificSector?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            QuickActions(contact, teacher, context)
        }

        if (state.isLoading) {
            LinearWavyProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        ContactsGroup(contact, context, copy)

        AnimatedVisibility(visible = teacher != null, enter = fadeIn() + expandVertically()) {
            if (teacher != null) {
                Column { TeacherSections(teacher, onOpen = { context.openContactUri(it) }) }
            }
        }

        RoleGroup("Ruoli", contact.roles, Icons.Rounded.Badge)
        RoleGroup("Incarichi", contact.functions, Icons.Rounded.Work)
    }
}

@Composable
private fun ContactsGroup(contact: Contact, context: Context, copy: (String) -> Unit) {
    val rows = buildList {
        contact.emails.forEach { add(ContactValue(Icons.Rounded.Mail, it, "Email", "mailto:$it")) }
        contact.certifiedEmails.forEach { add(ContactValue(Icons.Rounded.MarkEmailRead, it, "PEC", "mailto:$it")) }
        contact.phones.forEach { add(ContactValue(Icons.Rounded.Call, it, "Telefono", "tel:${it.dialable()}")) }
        contact.websites.forEach { add(ContactValue(Icons.Rounded.Language, it, "Sito web", if (it.startsWith("http")) it else "https://$it")) }
        contact.offices.forEach { add(ContactValue(Icons.Rounded.MeetingRoom, it, "Ufficio", null)) }
    }
    if (rows.isEmpty()) return
    GroupHeader("Contatti")
    rows.forEachIndexed { index, row ->
        GroupedListItem(
            title = row.value,
            supporting = row.label,
            icon = row.icon,
            index = index,
            count = rows.size,
            onClick = row.uri?.let { uri -> { context.openContactUri(uri) } },
            trailing = {
                IconButton(onClick = { copy(row.value) }) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copia")
                }
            }
        )
    }
}

@Composable
private fun RoleGroup(title: String, roles: List<ContactRole>, icon: ImageVector) {
    if (roles.isEmpty()) return
    GroupHeader(title)
    roles.forEachIndexed { index, role ->
        GroupedListItem(
            title = role.title,
            supporting = role.structure,
            icon = icon,
            index = index,
            count = roles.size
        )
    }
}

@Composable
private fun QuickActions(contact: Contact, teacher: TeacherProfile?, context: Context) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        contact.emails.firstOrNull()?.let { email ->
            ActionButton("Email", Icons.Rounded.Mail) { context.openContactUri("mailto:$email") }
        }
        contact.phones.firstOrNull()?.let { phone ->
            ActionButton("Chiama", Icons.Rounded.Call) { context.openContactUri("tel:${phone.dialable()}") }
        }
        teacher?.let {
            ActionButton("Pagina docente", Icons.Rounded.Language) { context.openContactUri(it.pageUrl) }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(label)
    }
}

private data class ContactValue(val icon: ImageVector, val value: String, val label: String, val uri: String?)

/** "0984/000000" -> "0984493921": dialers don't like separators. */
private fun String.dialable(): String = filter { it.isDigit() || it == '+' }

internal fun Context.openContactUri(uri: String) {
    if (uri.startsWith("http")) {
        openInCustomTab(uri)
        return
    }
    try {
        startActivity(Intent(if (uri.startsWith("tel:")) Intent.ACTION_DIAL else Intent.ACTION_SENDTO, uri.toUri()))
    } catch (_: ActivityNotFoundException) {
        // No mail or phone app: nothing sensible to do
    }
}
