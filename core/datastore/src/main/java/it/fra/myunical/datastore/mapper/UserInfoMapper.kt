package it.fra.myunical.datastore.mapper

import it.fra.myunical.datastore.models.CareerTrackDataModel
import it.fra.myunical.datastore.models.UserInfoDataModel
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.UserInfoModel


fun UserInfoModel.toDataModel() = UserInfoDataModel(
    username = username,
    firstName = firstName,
    lastName = lastName,
    persId = persId,
    careers = careers.map(CareerTrackModel::toDataModel)
)

fun UserInfoDataModel.toDomainModel() = UserInfoModel(
    username = username,
    firstName = firstName,
    lastName = lastName,
    persId = persId,
    careers = careers.map(CareerTrackDataModel::toDomainModel)
)