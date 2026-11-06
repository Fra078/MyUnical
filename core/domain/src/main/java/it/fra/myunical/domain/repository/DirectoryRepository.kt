package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactPage
import it.fra.myunical.domain.model.directory.TeacherProfile

/** University address book and public teacher pages. */
interface DirectoryRepository {
    suspend fun search(query: String, page: Int): ContactPage

    /** A contact seen in search results, to show its detail while the full record loads. */
    suspend fun cached(id: String): Contact?

    suspend fun getContact(id: String): Contact

    /** Teacher page with the courses of the current academic year. */
    suspend fun getTeacherProfile(id: String): TeacherProfile
}
