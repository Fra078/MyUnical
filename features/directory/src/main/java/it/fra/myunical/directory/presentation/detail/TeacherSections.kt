package it.fra.myunical.directory.presentation.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.fra.myunical.directory.presentation.search.InitialsAvatar
import it.fra.myunical.domain.model.directory.TaughtCourse
import it.fra.myunical.domain.model.directory.TeacherProfile
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.GroupedListItem

private const val PHOTO_SIZE = 112

/** Teacher photo in an expressive shape; the initials stay underneath until it loads. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ContactPhoto(photoUrl: String?, initials: String) {
    Box(Modifier.size(PHOTO_SIZE.dp), contentAlignment = Alignment.Center) {
        InitialsAvatar(initials, size = PHOTO_SIZE)
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto del docente",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialShapes.Cookie6Sided.toShape())
            )
        }
    }
}

/** Sections from the public teacher page: office hours, teaching, bio, research and publications. */
@Composable
internal fun TeacherSections(teacher: TeacherProfile, onOpen: (String) -> Unit) {
    teacher.receptionHoursHtml?.let {
        GroupHeader("Ricevimento")
        HtmlCard(it, collapsedLines = null)
    }

    if (teacher.courses.isNotEmpty()) {
        GroupHeader("Insegnamenti A.A. ${teacher.academicYear}/${(teacher.academicYear + 1) % 100}")
        teacher.courses.forEachIndexed { index, course ->
            GroupedListItem(
                title = course.name,
                supporting = course.details(),
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                index = index,
                count = teacher.courses.size
            )
        }
    }

    teacher.bioHtml?.let {
        GroupHeader("Biografia")
        HtmlCard(it, collapsedLines = 4)
    }

    if (teacher.researchLines.isNotEmpty()) {
        GroupHeader("Linee di ricerca")
        teacher.researchLines.forEachIndexed { index, line ->
            GroupedListItem(
                title = line,
                icon = Icons.Rounded.Science,
                index = index,
                count = teacher.researchLines.size
            )
        }
    }

    if (teacher.publications.isNotEmpty()) {
        GroupHeader("Pubblicazioni recenti")
        val showAll = teacher.publicationCount > teacher.publications.size
        val count = teacher.publications.size + if (showAll) 1 else 0
        teacher.publications.forEachIndexed { index, publication ->
            GroupedListItem(
                title = publication.title,
                supporting = listOfNotNull(publication.year?.toString(), publication.type).joinToString(" · ").ifBlank { null },
                icon = Icons.Rounded.Article,
                index = index,
                count = count,
                onClick = publication.url?.let { url -> { onOpen(url) } }
            )
        }
        if (showAll) {
            GroupedListItem(
                title = "Tutte le ${teacher.publicationCount} pubblicazioni",
                index = count - 1,
                count = count,
                titleColor = MaterialTheme.colorScheme.primary,
                onClick = { onOpen(teacher.pageUrl) },
                trailing = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) }
            )
        }
    }

    val links = buildList {
        teacher.cvUrl?.let { add(Triple("Curriculum vitae", "PDF", Icons.Rounded.Description) to it) }
        teacher.orcid?.let { add(Triple("ORCID", it, Icons.Rounded.Fingerprint) to "https://orcid.org/$it") }
    }
    if (links.isNotEmpty()) {
        GroupHeader("Profilo")
        links.forEachIndexed { index, (info, url) ->
            GroupedListItem(
                title = info.first,
                supporting = info.second,
                icon = info.third,
                index = index,
                count = links.size,
                onClick = { onOpen(url) },
                trailing = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null) }
            )
        }
    }
}

/** "Fisica · 2° anno · Secondo Semestre · Inglese" */
private fun TaughtCourse.details(): String? =
    listOfNotNull(degreeCourse, year?.let { "$it° anno" }, semester, language?.takeIf { it != "Italiano" })
        .joinToString(" · ")
        .ifBlank { null }

/** HTML text from the university site; long texts collapse to [collapsedLines] with a toggle. */
@Composable
private fun HtmlCard(html: String, collapsedLines: Int?) {
    val linkColor = MaterialTheme.colorScheme.primary
    val text = remember(html, linkColor) {
        val parsed = AnnotatedString.fromHtml(
            html.trim(),
            linkStyles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
        )
        // Paragraphs leave trailing new lines that would show as empty space
        parsed.subSequence(0, parsed.text.trimEnd().length)
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var overflows by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .animateContentSize(spring(stiffness = 380f))
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (overflows || expanded) 4.dp else 14.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (collapsedLines == null || expanded) Int.MAX_VALUE else collapsedLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
            )
            if (overflows || expanded) {
                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.align(Alignment.End)) {
                    Text(if (expanded) "Mostra meno" else "Mostra tutto")
                }
            }
        }
    }
}
