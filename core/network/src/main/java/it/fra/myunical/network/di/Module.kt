package it.fra.myunical.network.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.network.sources.AuthRemoteDataSourceImpl
import it.fra.myunical.network.sources.CinecaRemoteDataSourceImpl
import it.fra.myunical.network.sources.NewsRemoteDataSourceImpl
import it.fra.myunical.network.sources.UnicalStorageDataSourceImpl
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val BaseUrlKey = AttributeKey<String>("BaseUrlKey")
val networkModule = module {

    single<HttpClient>(named("CinecaClient")){
        val authLocalDataSource : AuthLocalDataSource = get()
        HttpClient(OkHttp){

            defaultRequest {
                url("https://unical.esse3.cineca.it/e3rest/api/")
            }
            install(ContentNegotiation){
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Logging){
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = io.ktor.client.plugins.logging.LogLevel.ALL
            }
            install(Auth){
                basic {
                    credentials {
                        val credentials = authLocalDataSource.getCredentials() ?: return@credentials null
                        BasicAuthCredentials(
                            username = credentials.username,
                            password = credentials.password
                        )
                    }
                    sendWithoutRequest { request ->
                        true
                    }
                }

            }
        }.apply { attributes.put(BaseUrlKey, "https://unical.esse3.cineca.it/e3rest/api/") }
    }

    single<HttpClient>(named("CinecaImageClient")){
        val authLocalDataSource : AuthLocalDataSource = get()
        HttpClient(OkHttp){
            install(Auth){
                basic {
                    credentials {
                        val credentials = authLocalDataSource.getCredentials() ?: return@credentials null
                        BasicAuthCredentials(
                            username = credentials.username,
                            password = credentials.password
                        )
                    }
                    sendWithoutRequest { request ->
                        request.url.host.contains("unical.esse3.cineca.it")
                    }
                }

            }
        }.apply { attributes.put(BaseUrlKey, "https://unical.esse3.cineca.it/e3rest/api/") }
    }

    single<AuthRemoteDataSource> {
        AuthRemoteDataSourceImpl(
            HttpClient(OkHttp) {
                defaultRequest {
                    url("https://unical.esse3.cineca.it/e3rest/api/")
                }
                install(Logging){
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            println("LOGINTERCEPTOR: " + message)
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                           ignoreUnknownKeys = true
                           isLenient = true
                        }
                    )
                }
            }
        )
    }

    single<NewsRemoteDataSource> {
        NewsRemoteDataSourceImpl(
            httpClient = HttpClient(OkHttp) {
                defaultRequest {
                    url("https://www.unical.it/api/")
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                        }
                    )
                }
            }
        )
    }

    single<UnicalStorageDataSource> {
        UnicalStorageDataSourceImpl(
            httpClient = HttpClient(OkHttp) {
                defaultRequest {
                    url("https://storage.portale.unical.it/api/")
                }
                install(Logging){
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            println("UNISTORAGE: " + message)
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                        }
                    )
                }
            }
        )
    }

    single<CinecaRemoteDataSource> {
        CinecaRemoteDataSourceImpl(get(named("CinecaClient")))
    }
}