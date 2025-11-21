package it.appm.myunical.model

import com.google.gson.annotations.SerializedName

data class InfoAppello(
    @SerializedName("adDes") val titolo: String,
    @SerializedName("adCod") val disciplinaCod: String,
    @SerializedName("appId") val appelloId: Int,
    @SerializedName("adId")  val disciplinaId: Int,
    @SerializedName("turni") val turni: List<Turno>
){
    data class Turno(
        @SerializedName("aulaDes") val aula: String,
        @SerializedName("des") val description: String,
        @SerializedName("dataOraEsa") val dataOraEsa: String,
    )
}