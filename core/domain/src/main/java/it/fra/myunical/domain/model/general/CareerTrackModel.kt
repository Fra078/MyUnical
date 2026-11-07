package it.fra.myunical.domain.model.general

data class CareerTrackModel(
    val name: String,
    val matId: Int,
    val stuId: Int,
    val matricola: String,
    val active: Boolean,
    val closedReason: String?,
    /** Esse3 degree course code ("0819"), shared with the course catalogue; null in careers saved by older versions. */
    val cdsCode: String? = null
)

/** Short status shown next to the career name: "Attiva", "Titolo conseguito", "Chiusa · <motivo>". */
val CareerTrackModel.statusLabel: String
    get() = when {
        active -> "Attiva"
        closedReason?.contains("titolo", ignoreCase = true) == true -> "Titolo conseguito"
        else -> closedReason?.let { "Chiusa · $it" } ?: "Chiusa"
    }
