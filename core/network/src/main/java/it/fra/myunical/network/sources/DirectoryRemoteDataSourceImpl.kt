package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPathPart
import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactPage
import it.fra.myunical.domain.model.directory.TeacherProfile
import it.fra.myunical.domain.sources.DirectoryRemoteDataSource
import it.fra.myunical.network.mapper.toDomainModel
import it.fra.myunical.network.mapper.toTeacherProfile
import it.fra.myunical.network.models.directory.AddressBookDetailDto
import it.fra.myunical.network.models.directory.AddressBookPageDto
import it.fra.myunical.network.models.directory.PublicationPageDto
import it.fra.myunical.network.models.directory.ResearchLinePageDto
import it.fra.myunical.network.models.directory.StudyActivityPageDto
import it.fra.myunical.network.models.directory.TeacherDetailDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DirectoryRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : DirectoryRemoteDataSource {

    override suspend fun search(query: String, page: Int): ContactPage =
        httpClient.get("ricerca/addressbook/") {
            parameter("search", query)
            parameter("page", page)
            parameter("page_size", PAGE_SIZE)
        }.body<AddressBookPageDto>().toDomainModel()

    override suspend fun getContact(id: String): Contact =
        httpClient.get("ricerca/addressbook/${id.encodeURLPathPart()}/")
            .body<AddressBookDetailDto>()
            .results?.toDomainModel()
            ?: throw NoSuchElementException("Contact $id not found")

    override suspend fun getTeacherProfile(id: String, academicYear: Int): TeacherProfile = coroutineScope {
        val path = "ricerca/teachers/${id.encodeURLPathPart()}"
        // Optional parts: a failure leaves the section empty instead of failing the page
        val activities = async {
            runCatching {
                httpClient.get("ricerca/activities/") {
                    parameter("teacher_matricola", id)
                    parameter("academic_year", academicYear)
                    parameter("lang", "it")
                }.body<StudyActivityPageDto>()
            }.getOrNull()
        }
        val researchLines = async { runCatching { httpClient.get("$path/researchlines/").body<ResearchLinePageDto>() }.getOrNull() }
        val publications = async {
            runCatching {
                httpClient.get("$path/publications/") { parameter("page_size", PUBLICATIONS_SHOWN) }.body<PublicationPageDto>()
            }.getOrNull()
        }
        val teacher = httpClient.get("$path/") { parameter("lang", "it") }.body<TeacherDetailDto>().results
            ?: throw NoSuchElementException("Teacher $id not found")
        teacher.toTeacherProfile(id, academicYear, activities.await(), researchLines.await(), publications.await())
    }

    private companion object {
        const val PUBLICATIONS_SHOWN = 5
        const val PAGE_SIZE = 25
    }
}
