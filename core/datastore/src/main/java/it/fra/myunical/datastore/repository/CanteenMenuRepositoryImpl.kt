package it.fra.myunical.datastore.repository

import it.fra.myunical.datastore.mapper.toDomainModel
import it.fra.myunical.datastore.models.CanteenMenuDataModel
import it.fra.myunical.domain.model.canteen.CanteenMenu
import it.fra.myunical.domain.repository.CanteenMenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream

/**
 * Menus bundled with the app ([open] reads `assets/canteen/menu.json`), parsed once per process.
 * A remote source can be added here later, keeping the asset as fallback.
 */
class CanteenMenuRepositoryImpl(
    private val json: Json,
    private val open: () -> InputStream
) : CanteenMenuRepository {

    private var cached: CanteenMenu? = null
    private val mutex = Mutex()

    override suspend fun getMenu(): CanteenMenu = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            open().bufferedReader().use { json.decodeFromString<CanteenMenuDataModel>(it.readText()) }
        }.toDomainModel().also { cached = it }
    }

    companion object {
        const val ASSET = "canteen/menu.json"
    }
}
