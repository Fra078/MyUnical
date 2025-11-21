package it.appm.myunical.model

import com.google.gson.annotations.SerializedName

data class Carriera(
    @SerializedName("nome") val nome: String,
    @SerializedName("cognome") val cognome: String,
    @SerializedName("matricola") val matricola: String,
    @SerializedName("annoCorso") val annoCds: Int,
    @SerializedName("aaIscrId") val annoAccademico: Int,
    @SerializedName("p06CdsCod") val cdsCod: String,
    @SerializedName("cdsId") val cdsId: Int,
    @SerializedName("p06CdsDes") val facolta: String,
    @SerializedName("matId") val matId: Int,
    @SerializedName("persId") val persId: Int
)
