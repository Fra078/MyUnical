package it.fra.myunical.network.models.taxes

import it.fra.myunical.network.models.LenientDoubleSerializer
import kotlinx.serialization.Serializable

/**
 * Row of `tasse-service-v1/addebiti-studente`: one fee item, repeated for every item of an invoice.
 * The published spec is unreliable, so every field is optional; dates are `dd/MM/yyyy[ HH:mm:ss]`,
 * empty or the literal "null".
 */
@Serializable
data class StudentChargeDto(
    val stuId: Long? = null,
    val aaId: Int? = null,
    val semaforo: String? = null,
    @Serializable(with = LenientDoubleSerializer::class) val dovuto: Double? = null,
    val visWebFlg: Int? = null,
    val annullataFlg: Int? = null,
    val fattAnnullata: Long? = null,
    val tassaId: Long? = null,
    val tassaDes: String? = null,
    val voceDes: String? = null,
    @Serializable(with = LenientDoubleSerializer::class) val importoVoce: Double? = null,
    val rataId: Long? = null,
    val rataDes: String? = null,
    val scadenzaAddebito: String? = null,
    val fattId: Long? = null,
    val scadFattura: String? = null,
    @Serializable(with = LenientDoubleSerializer::class) val importoFattura: Double? = null,
    val desMav1: String? = null,
    val pagatoFlg: Int? = null,
    @Serializable(with = LenientDoubleSerializer::class) val importoPag: Double? = null,
    val dataPagamento: String? = null,
    val codiceAvviso: String? = null
)
