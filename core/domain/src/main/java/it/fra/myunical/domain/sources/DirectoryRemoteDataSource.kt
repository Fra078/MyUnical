package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactPage
import it.fra.myunical.domain.model.directory.TeacherProfile

interface DirectoryRemoteDataSource {
    suspend fun search(query: String, page: Int): ContactPage
    suspend fun getContact(id: String): Contact

    /**
     * Teacher page with the courses of [academicYear]. Only the profile itself is required:
     * courses, research lines and publications are empty when they can't be loaded.
     */
    suspend fun getTeacherProfile(id: String, academicYear: Int): TeacherProfile
}
