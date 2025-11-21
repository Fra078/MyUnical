package it.appm.myunical.data.cineca

import android.adservices.adid.AdId
import it.appm.myunical.data.room.LibrettoItem
import it.appm.myunical.model.Carriera
import it.appm.myunical.model.InfoAppello
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CinecaClient(
    val codiceFiscale : String,
    val token : String
) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://unical.esse3.cineca.it/e3rest/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val client = retrofit.create(CinecaService::class.java)

    suspend fun login() : LoginState {
        return try {
            val response = client.login(token)
            if (response.isSuccessful)
                LoginState.READY
            else
                LoginState.LOGIN_FAILED_CAUSE_WRONG_CREDENTIALS
        } catch (exc: Throwable){
            LoginState.LOGIN_FAILED_CAUSE_NO_CONNECTION
        }
    }

    suspend fun getEsamiDisponibili(matId: Int, cdsId: Int) : List<InfoAppello>? {
        try {
            //val esamiDisponibili = client.getEsamiDisponibili(token).body() ?: return null
            val prenotazioni = client.getPrenotazioni(token, matId).body()?: return null
            return prenotazioni.mapNotNull {
                client.getInfoAppello(token, cdsId, it.idDisciplina, it.idAppello).body()
            }

        } catch (ecx: Exception){
            return null
        }
    }

    suspend fun getCarriera() : Result<Carriera>{
        try {
            val response = client.getCarriere(token, codiceFiscale)
            if (response.isSuccessful){
                println(response.body())
                val carriera = response.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Errore nel caricamento del tesserino"))

                return Result.success(carriera)
            } else {
                return Result.failure(Exception("Errore ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (exception : Exception){
            println(exception)
            return Result.failure(exception)
        }
    }

    suspend fun downloadUserPhoto(persId: Int, file: File): Result<Unit> {
        val photoSrc = try {
            println("Downloading photo")
            client.getPhoto(token, persId).body() ?: return Result.failure(Exception("Nessun dato trovato"))
        } catch (exc: Exception){
            return Result.failure(exc)
        }
        file.outputStream().use { output ->
            photoSrc.byteStream().use { input ->
                input.copyTo(output)
            }
        }
        return Result.success(Unit)
    }

    suspend fun getLibretto(matId: Int) : Result<List<LibrettoItem>>{
        try {
            val response = client.getLibretto(token, matId).body() ?: return Result.failure(Exception("Impossibile scaricare il libretto"))
            return response.map {
                val id = (it["adsceId"] as Double).toInt()
                val title = it["adDes"] as String
                val cfu = (it["peso"] as Double).toInt()
                val esito = it["esito"] as Map<*, *>
                val vote = (esito["voto"] as Double?)?.toInt()
                val laude = (esito["lodeFlg"] as Double).toInt()
                val dataEsa = (esito["dataEsa"] as String)
                LibrettoItem(id, title, cfu, dataEsa, vote, laude > 0)
            }.let { Result.success(it) }

        } catch (exc: Exception){
            return Result.failure(exc)
        }

    }

    companion object {
        @OptIn(ExperimentalEncodingApi::class)
        fun fromPassword(codiceFiscale: String, password: String) : CinecaClient {
            val upseq = "$codiceFiscale:$password"
            val token = "Basic " + Base64.encode(upseq.toByteArray(Charsets.UTF_8))
            return CinecaClient(codiceFiscale, token)
        }
    }
}