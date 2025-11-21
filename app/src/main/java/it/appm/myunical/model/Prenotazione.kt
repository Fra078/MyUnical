package it.appm.myunical.model

import com.google.gson.annotations.SerializedName

data class Prenotazione(
    @SerializedName("adId") val idDisciplina: Int,
    @SerializedName("appId") val idAppello: Int,
)