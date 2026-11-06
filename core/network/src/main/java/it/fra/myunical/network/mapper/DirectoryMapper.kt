package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactPage
import it.fra.myunical.domain.model.directory.ContactRole
import it.fra.myunical.domain.model.directory.Publication
import it.fra.myunical.domain.model.directory.TaughtCourse
import it.fra.myunical.domain.model.directory.TeacherProfile
import it.fra.myunical.network.models.directory.AddressBookPageDto
import it.fra.myunical.network.models.directory.AddressBookPersonDto
import it.fra.myunical.network.models.directory.PublicationPageDto
import it.fra.myunical.network.models.directory.ResearchLinePageDto
import it.fra.myunical.network.models.directory.StudyActivityPageDto
import it.fra.myunical.network.models.directory.TeacherDto

internal fun AddressBookPageDto.toDomainModel(): ContactPage = ContactPage(
    items = results.mapNotNull { it.toDomainModel() },
    page = pageNumber,
    hasNext = next != null,
    totalCount = count
)

internal fun AddressBookPersonDto.toDomainModel(): Contact? {
    val id = id.clean() ?: return null
    return Contact(
        id = id,
        name = name.clean()?.toDisplayName() ?: id,
        roles = roles
            .sortedBy { it.priority ?: Int.MAX_VALUE }
            .mapNotNull { role ->
                ContactRole(
                    title = role.description.clean() ?: return@mapNotNull null,
                    structure = role.structure.clean()?.toTitleCase()
                )
            },
        emails = emails.mapNotNull { it.clean() },
        certifiedEmails = pecs.mapNotNull { it.clean() },
        phones = (officePhones + mobilePhones).mapNotNull { it.clean() }.distinct(),
        websites = websites.mapNotNull { it.clean() },
        offices = offices.mapNotNull { it.clean() },
        functions = functions.mapNotNull { function ->
            ContactRole(
                title = function.role.clean() ?: return@mapNotNull null,
                structure = function.structure.clean()?.toTitleCase()
            )
        },
        isTeacher = teacher
    )
}

/**
 * The address book writes surnames in upper case before the first name, in either order:
 * "ROSSI Andrea" or "Andrea ROSSI" -> "Andrea Rossi". Fully upper case names
 * ("ROSSI GIOVANNI") keep their order since surname and first name can't be told apart.
 */
internal fun String.toDisplayName(): String {
    val words = trim().split(Regex("\\s+"))
    fun String.isUpper() = any(Char::isLetter) && this == uppercase()
    val surname = words.filter { it.isUpper() }
    val firstName = words.filterNot { it.isUpper() }
    val ordered = if (surname.isEmpty() || firstName.isEmpty()) words else firstName + surname
    return ordered.joinToString(" ") { it.capitalizeName() }
}

/** "D'AMICO" -> "D'Amico", "LO" -> "Lo", "DE-LUCA" -> "De-Luca". */
private fun String.capitalizeName(): String {
    val result = StringBuilder(length)
    var startOfPart = true
    for (char in this) {
        result.append(if (startOfPart) char.titlecaseChar() else char.lowercaseChar())
        startOfPart = !char.isLetter()
    }
    return result.toString()
}

internal fun TeacherDto.toTeacherProfile(
    id: String,
    academicYear: Int,
    activities: StudyActivityPageDto?,
    researchLines: ResearchLinePageDto?,
    publications: PublicationPageDto?
) = TeacherProfile(
    id = id,
    photoUrl = photoPath.toAbsoluteUrl(),
    scientificSector = sectorDescription.clean(),
    receptionHoursHtml = receptionHours.htmlOrNull(),
    bioHtml = shortBio.htmlOrNull(),
    cvUrl = (cvPathIt ?: cvPathEn).toAbsoluteUrl(),
    orcid = orcid.clean()?.takeIf { it.any(Char::isDigit) },
    courses = activities?.results.orEmpty()
        .mapNotNull { activity ->
            TaughtCourse(
                name = activity.name.clean()?.toTitleCase() ?: return@mapNotNull null,
                degreeCourse = activity.degreeCourse.clean()?.toTitleCase(),
                year = activity.year?.toInt(),
                semester = activity.semester.clean(),
                language = activity.language.clean()
            )
        }
        // A course shared by several study plans appears once per plan
        .distinctBy { it.name to it.degreeCourse },
    academicYear = academicYear,
    researchLines = researchLines?.results.orEmpty().mapNotNull { it.description.clean() }.distinct(),
    publications = publications?.results.orEmpty().mapNotNull { publication ->
        Publication(
            title = publication.title.clean() ?: return@mapNotNull null,
            year = publication.year,
            type = publication.label.clean(),
            url = publication.url.toAbsoluteUrl()
        )
    },
    publicationCount = publications?.count ?: 0,
    pageUrl = "https://www.unical.it/storage/teachers/$id/"
)

/** The API writes scheme-relative URLs: "//storage.portale.unical.it/..." -> "https://...". */
internal fun String?.toAbsoluteUrl(): String? = clean()?.let {
    when {
        it.startsWith("//") -> "https:$it"
        it.startsWith("http") -> it
        else -> null
    }
}

/** Drops HTML that renders as nothing, like "<p>&nbsp;</p>". */
private fun String?.htmlOrNull(): String? = clean()?.takeIf { html ->
    html.replace(Regex("<[^>]*>"), "").replace("&nbsp;", "").isNotBlank()
}
