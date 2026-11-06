package it.fra.myunical.domain.model.directory

/** A person in the university address book (teachers, staff, research fellows). */
data class Contact(
    val id: String,
    val name: String,
    val roles: List<ContactRole>,
    val emails: List<String>,
    val certifiedEmails: List<String>,
    val phones: List<String>,
    val websites: List<String>,
    /** Office location, e.g. "Cubo 31C Piano 5 Stanza 6". */
    val offices: List<String> = emptyList(),
    /** Institutional duties, only available in the detail. */
    val functions: List<ContactRole> = emptyList(),
    val isTeacher: Boolean = false
)

/** A role ("Professore Associato") held in a structure ("Dipartimento di Informatica..."). */
data class ContactRole(
    val title: String,
    val structure: String?
)

data class ContactPage(
    val items: List<Contact>,
    val page: Int,
    val hasNext: Boolean,
    val totalCount: Int
)

/** Public teacher page: photo, teaching and research, as shown on www.unical.it. */
data class TeacherProfile(
    val id: String,
    val photoUrl: String?,
    val scientificSector: String?,
    val receptionHoursHtml: String?,
    val bioHtml: String?,
    val cvUrl: String?,
    val orcid: String?,
    val courses: List<TaughtCourse>,
    /** Academic year of [courses], identified by its starting year. */
    val academicYear: Int,
    val researchLines: List<String>,
    val publications: List<Publication>,
    val publicationCount: Int,
    val pageUrl: String
)

data class TaughtCourse(
    val name: String,
    val degreeCourse: String?,
    val year: Int?,
    val semester: String?,
    val language: String?
)

data class Publication(
    val title: String,
    val year: Int?,
    val type: String?,
    val url: String?
)
