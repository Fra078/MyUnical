package it.appm.myunical.data2.folder

import it.appm.myunical.data.cineca.CinecaService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CinecaClient2(private val username: String, private val token: String) {


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://unical.esse3.cineca.it/e3rest/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val client = retrofit.create(CinecaService::class.java)

    suspend fun login(){
        val response = client.login(token)
        if (response.isSuccessful){

        } else {
        }
    }


    companion object {
        @OptIn(ExperimentalEncodingApi::class)
        fun fromPassword(username: String, password: String) : CinecaClient2 {
            val upseq = "$username:$password"
            val token = "Basic " + Base64.encode(upseq.toByteArray(Charsets.UTF_8))
            return CinecaClient2(username, token)
        }
    }

    sealed interface LoginResponse {
        data class Success(val username: String, )
    }

}