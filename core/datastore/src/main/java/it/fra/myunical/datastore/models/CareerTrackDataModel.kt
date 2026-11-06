package it.fra.myunical.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class CareerTrackDataModel(
    val name: String,
    val matId: Int,
    val stuId: Int,
    val matricola: String,
    val active: Boolean,
    val closedReason: String?
)
