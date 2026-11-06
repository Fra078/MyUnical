package it.fra.myunical.network.models.cineca

import it.fra.myunical.network.models.LenientDoubleSerializer
import kotlinx.serialization.Serializable

/** `libretto-service-v2/libretti/{matId}/stats`; credits are expressed in `umPeso*` fields. */
@Serializable
data class LibrettoStatsDto(
    @Serializable(with = LenientDoubleSerializer::class) val umPesoMin: Double? = null,
    @Serializable(with = LenientDoubleSerializer::class) val umPesoSuperato: Double? = null,
    val numAdSuperate: Int? = null,
    val numAdLibretto: Int? = null
)

/** `libretto-service-v2/libretti/{matId}/medie`: one row per base (30, 110) and type (A = arithmetic, P = weighted). */
@Serializable
data class LibrettoAverageDto(
    val base: Int? = null,
    val tipoMediaCod: ValueObjectDto? = null,
    @Serializable(with = LenientDoubleSerializer::class) val media: Double? = null
)

/** `libretto-service-v2/libretti/{matId}/appelli` */
@Serializable
data class ExamSessionDto(
    val appId: Long? = null,
    val adsceId: Long? = null,
    val adDes: String? = null,
    val desApp: String? = null,
    val dataInizioApp: String? = null,
    val oraEsa: String? = null,
    val dataInizioIscr: String? = null,
    val dataFineIscr: String? = null,
    val stato: String? = null,
    val presidenteNome: String? = null,
    val presidenteCognome: String? = null
)

/** `libretto-service-v2/libretti/{matId}/prenotazioni` (IscrizioneAppello) */
@Serializable
data class ExamBookingDto(
    val applistaId: Long? = null,
    val appId: Long? = null,
    val adStuDes: String? = null,
    val desAppello: String? = null,
    val dataOraTurno: String? = null,
    val adsceId: Long? = null,
    val posiz: Int? = null,
    val aulaDes: String? = null,
    val edificioDes: String? = null
)
