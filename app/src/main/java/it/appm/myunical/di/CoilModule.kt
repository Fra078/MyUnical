@file:OptIn(ExperimentalCoilApi::class)

package it.appm.myunical.di

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import it.fra.myunical.network.di.HttpClients
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coilModule = module {

    single<ImageLoader> {
        val httpClient = get<HttpClient>(HttpClients.ESSE3_IMAGES)

        ImageLoader.Builder(androidContext())
            .components {
                add(KtorNetworkFetcherFactory(httpClient))
            }
            //.respectCacheHeaders(false)
            .build()
    }

}