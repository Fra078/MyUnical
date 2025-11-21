package it.appm.myunical.data.cineca

import it.appm.myunical.model.Carriera
import it.appm.myunical.model.EsamiDisponibili
import it.appm.myunical.model.InfoAppello
import it.appm.myunical.model.Prenotazione
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface CinecaService {
    @GET("login")
    @Headers("accept: application/json", "X-Esse3-permit-invalid-jsessionid: true")
    suspend fun login(
        @Header("Authorization") authorization: String
    ): Response<Map<String, Any>>

    @GET("carriere-service-v1/carriere")
    suspend fun getCarriere(
        @Header("Authorization") authorization: String,
        @Query("codFis") codFis: String
    ): Response<List<Carriera>>

    @GET("calesa-service-v1/prenotazioni/{matId}")
    suspend fun getPrenotazioni(
        @Header("Authorization") authorization: String,
        @Path("matId") matId: Int
    ) : Response<List<Prenotazione>>

    @GET("calesa-service-v1/appelli/{cdsId}/{adId}/{appId}")
    suspend fun getInfoAppello(
        @Header("Authorization") authorization: String,
        @Path("cdsId") cdsId: Int,
        @Path("adId") adId: Int,
        @Path("appId") appId: Int
    ) : Response<InfoAppello>

    @GET("calesa-service-v1/appelli")
    suspend fun getEsamiDisponibili(
        @Header("Authorization") authorization: String,
    ) : Response<List<EsamiDisponibili>>

    @GET("anagrafica-service-v2/persone/{persId}/foto")
    suspend fun getPhoto(
        @Header("Authorization") authorization: String,
        @Path("persId") persId: Int
    ): Response<ResponseBody>

    @GET("libretto-service-v2/libretti/{matId}/righe")
    suspend fun getLibretto(
        @Header("Authorization") authorization: String,
        @Path("matId") matId: Int
    ): Response<List<Map<String, Any>>>


}