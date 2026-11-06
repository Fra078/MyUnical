package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.auth.AuthError
import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.network.mapper.toUserInfoModel
import it.fra.myunical.network.models.AuthResponse
import kotlinx.coroutines.CancellationException

class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : AuthRemoteDataSource {

    override suspend fun validateCredentials(credentials: UserCredentials): AuthResponseModel = try {
        val response = httpClient.get("login") {
            basicAuth(credentials.username, credentials.password)
        }

        when(response.status){
            HttpStatusCode.OK -> AuthResponseModel.Success(response.body<AuthResponse>().user.toUserInfoModel())
            HttpStatusCode.Unauthorized -> AuthResponseModel.Failure(AuthError.InvalidCredentials)
            else -> AuthResponseModel.Failure(AuthError.Unknown(
                IllegalStateException("HTTP ${response.status.value}"))
            )
        }

    } catch (e: CancellationException){
        throw e
    } catch (e: Exception){
        AuthResponseModel.Failure(AuthError.Unknown(e))
    }

}