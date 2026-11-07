package it.fra.myunical.datastore.mapper

import it.fra.myunical.datastore.models.CareerTrackDataModel
import it.fra.myunical.domain.model.general.CareerTrackModel

fun CareerTrackModel.toDataModel() = CareerTrackDataModel(
    name = name,
    matId = matId,
    stuId = stuId,
    matricola = matricola,
    active = active,
    closedReason = closedReason,
    cdsCode = cdsCode
)

fun CareerTrackDataModel.toDomainModel() = CareerTrackModel(
    name = name,
    matId = matId,
    stuId = stuId,
    matricola = matricola,
    active = active,
    closedReason = closedReason,
    cdsCode = cdsCode
)