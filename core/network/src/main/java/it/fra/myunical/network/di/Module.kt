package it.fra.myunical.network.di

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import it.fra.myunical.domain.repository.DirectoryRepository
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.domain.sources.DirectoryRemoteDataSource
import it.fra.myunical.domain.sources.DocumentRemoteDataSource
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.domain.sources.TaxRemoteDataSource
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.network.BuildConfig
import it.fra.myunical.network.repository.DirectoryRepositoryImpl
import it.fra.myunical.network.repository.NewsRepositoryImpl
import it.fra.myunical.network.sources.AuthRemoteDataSourceImpl
import it.fra.myunical.network.sources.CinecaRemoteDataSourceImpl
import it.fra.myunical.network.sources.DirectoryRemoteDataSourceImpl
import it.fra.myunical.network.sources.DocumentRemoteDataSourceImpl
import it.fra.myunical.network.sources.NewsRemoteDataSourceImpl
import it.fra.myunical.network.sources.TaxRemoteDataSourceImpl
import it.fra.myunical.network.sources.UnicalStorageDataSourceImpl
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Base URLs of the services the app talks to. */
internal object Endpoints {
    const val ESSE3 = "https://unical.esse3.cineca.it/e3rest/api/"
    const val UNICAL_SITE = "https://www.unical.it/api/"
    const val UNICAL_STORAGE = "https://storage.portale.unical.it/api/"
}

/** Koin qualifiers of the HTTP clients; [ESSE3_IMAGES] is used by the app's image loader. */
object HttpClients {
    val ESSE3 = named("Esse3Client")
    val ESSE3_IMAGES = named("Esse3ImageClient")
    val UNICAL_SITE = named("UnicalSiteClient")
    val UNICAL_STORAGE = named("UnicalStorageClient")
}

// The APIs are loosely typed: unknown fields are skipped and nulls fall back to defaults
private val ApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Builds a client with the shared JSON setup. Traffic is logged only in debug builds, and
 * the Authorization header (Esse3 credentials) is never written to the log.
 */
private fun createHttpClient(
    tag: String,
    baseUrl: String? = null,
    configure: HttpClientConfig<*>.() -> Unit = {}
) = HttpClient(OkHttp) {
    baseUrl?.let { defaultRequest { url(it) } }
    install(ContentNegotiation) { json(ApiJson) }
    if (BuildConfig.DEBUG) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(tag, message)
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
    }
    configure()
}

/** Sends the saved Esse3 credentials, only to Esse3 hosts. */
private fun HttpClientConfig<*>.esse3BasicAuth(authLocalDataSource: AuthLocalDataSource) {
    install(Auth) {
        basic {
            credentials {
                val credentials = authLocalDataSource.getCredentials() ?: return@credentials null
                BasicAuthCredentials(username = credentials.username, password = credentials.password)
            }
            sendWithoutRequest { request -> request.url.host == "unical.esse3.cineca.it" }
        }
    }
}

val networkModule = module {

    single(HttpClients.ESSE3) {
        createHttpClient("Esse3", Endpoints.ESSE3) { esse3BasicAuth(get()) }
    }

    // No base URL: Coil passes absolute URLs (Esse3 photos and public images)
    single(HttpClients.ESSE3_IMAGES) {
        createHttpClient("Esse3Images") { esse3BasicAuth(get()) }
    }

    single(HttpClients.UNICAL_SITE) { createHttpClient("UnicalSite", Endpoints.UNICAL_SITE) }

    single(HttpClients.UNICAL_STORAGE) {
        createHttpClient("UnicalStorage", Endpoints.UNICAL_STORAGE) { expectSuccess = true }
    }

    // Login sends the credentials being verified explicitly, not the saved ones
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(createHttpClient("Esse3Login", Endpoints.ESSE3)) }

    single<CinecaRemoteDataSource> { CinecaRemoteDataSourceImpl(get(HttpClients.ESSE3)) }
    single<TaxRemoteDataSource> { TaxRemoteDataSourceImpl(get(HttpClients.ESSE3)) }
    single<DocumentRemoteDataSource> { DocumentRemoteDataSourceImpl(get(HttpClients.ESSE3)) }
    single<NewsRemoteDataSource> { NewsRemoteDataSourceImpl(get(HttpClients.UNICAL_SITE)) }
    single<UnicalStorageDataSource> { UnicalStorageDataSourceImpl(get(HttpClients.UNICAL_STORAGE)) }
    single<DirectoryRemoteDataSource> { DirectoryRemoteDataSourceImpl(get(HttpClients.UNICAL_STORAGE)) }

    // Repositories: single instances, they hold the in-memory caches
    single<NewsRepository> { NewsRepositoryImpl(get()) }
    single<DirectoryRepository> { DirectoryRepositoryImpl(get()) }
}
