package it.fra.myunical.directory.models

import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactRole
import it.fra.myunical.domain.model.directory.TeacherProfile

data class DirectoryUiState(
    val query: String = "",
    val contacts: List<Contact> = emptyList(),
    val totalCount: Int = 0,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = true,
    val hasError: Boolean = false
) {
    /** Searches start from this many characters, shorter queries match too many people. */
    val isQueryTooShort: Boolean get() = query.trim().length < MIN_QUERY_LENGTH

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}

sealed interface ContactDetailUiState {
    data object Loading : ContactDetailUiState
    data object Error : ContactDetailUiState

    /**
     * [isComplete] is false while the cached search result is shown and duties are loading.
     * [teacher] holds the public teacher page, loaded only for teachers.
     */
    data class Loaded(
        val contact: Contact,
        val isComplete: Boolean,
        val teacher: TeacherProfile? = null,
        val isTeacherLoading: Boolean = false
    ) : ContactDetailUiState {
        val isLoading: Boolean get() = !isComplete || isTeacherLoading
    }
}

/** Initials for the avatar: "Andrea Rossi" -> "AR". */
val Contact.initials: String
    get() = name.split(' ').filter { it.isNotBlank() }.let { words ->
        listOfNotNull(words.firstOrNull(), words.drop(1).lastOrNull())
            .joinToString("") { it.first().uppercase() }
    }

val ContactRole.label: String
    get() = listOfNotNull(title, structure).joinToString(" · ")
