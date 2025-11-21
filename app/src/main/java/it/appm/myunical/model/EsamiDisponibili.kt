package it.appm.myunical.model

import com.google.gson.annotations.SerializedName


data class EsamiDisponibili(
    @SerializedName("aaOffId") val annoId: Int,
    @SerializedName("adCod") val disciplinaCod: String,
    @SerializedName("adDefAppId") val disciplinaId: Int,
    @SerializedName("adDes") val titolo: String,
    @SerializedName("cdsDefAppId") val cdsId: Int
)
