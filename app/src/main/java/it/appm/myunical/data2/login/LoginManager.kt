package it.appm.myunical.data2.login

import it.appm.myunical.data.cineca.CinecaClient
import it.appm.myunical.data2.cinecaUrl
import it.appm.myunical.data2.folder.CinecaClient2
import it.unical.esse3.auth.api.AutenticazioneApi
import it.unical.esse3.auth.model.SessioneUtente
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.openapitools.client.infrastructure.ApiClient
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object LoginManager {

    suspend fun login(username: String, password: String) : SessioneUtente? {
        try {
            val okHttp =
                OkHttpClient.Builder().addInterceptor(AuthInterceptor(username, password)).build()
            val sessione = AutenticazioneApi(cinecaUrl, okHttp).login()
            return sessione;
        } catch (exc: Exception){
            exc.printStackTrace()
        }
        return null;
    }

    class AuthInterceptor(val username: String, val password: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            val basicCredential = Credentials.basic(username, password)
            requestBuilder.header("Authorization", basicCredential)
            val request = requestBuilder.build()
            return chain.proceed(request)
        }

    }
}