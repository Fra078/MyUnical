package it.fra.myunical.network.repository

import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.model.directory.ContactPage
import it.fra.myunical.domain.model.directory.TeacherProfile
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.repository.DirectoryRepository
import it.fra.myunical.domain.sources.DirectoryRemoteDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Contacts seen in search results are remembered, so their detail opens immediately while
 * the full record (with duties) is fetched. Teacher pages are cached for the session.
 */
internal class DirectoryRepositoryImpl(
    private val dataSource: DirectoryRemoteDataSource
) : DirectoryRepository {

    private val known = mutableMapOf<String, Contact>()
    private val teachers = mutableMapOf<String, TeacherProfile>()
    private val mutex = Mutex()

    override suspend fun search(query: String, page: Int): ContactPage {
        val result = dataSource.search(query.trim(), page)
        mutex.withLock { result.items.forEach { known.putIfAbsent(it.id, it) } }
        return result
    }

    override suspend fun cached(id: String): Contact? = mutex.withLock { known[id] }

    override suspend fun getContact(id: String): Contact {
        val contact = dataSource.getContact(id)
        mutex.withLock { known[id] = contact }
        return contact
    }

    override suspend fun getTeacherProfile(id: String): TeacherProfile {
        mutex.withLock { teachers[id] }?.let { return it }
        val profile = dataSource.getTeacherProfile(id, AcademicYear.current())
        mutex.withLock { teachers[id] = profile }
        return profile
    }
}
