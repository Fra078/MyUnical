package it.fra.myunical.domain.model.general

data class CareerTrackModel(
    val name: String,
    val matId: Int,
    val stuId: Int,
    val matricola: String,
    val active: Boolean,
    val closedReason: String?
)
