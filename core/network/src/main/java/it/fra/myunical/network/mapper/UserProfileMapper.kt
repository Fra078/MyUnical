package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.UserInfoModel
import it.fra.myunical.network.models.CareerTrack
import it.fra.myunical.network.models.UserProfile

fun UserProfile.toUserInfoModel() = UserInfoModel(
    username = fiscalCode,
    firstName = firstName,
    lastName = lastName,
    persId = personId.toInt(),
    careers = careerTracks.map(CareerTrack::toDomainModel)
)

fun CareerTrack.toDomainModel() = CareerTrackModel(
    name = degreeCourseDescription,
    matId = matriculationId.toInt(),
    stuId = studentId.toInt(),
    matricola = studentNumber,
    active = studentStatusCode != "X",
    closedReason = if (studentStatusCode != "X") null else studentStatusReasonDescription,
    cdsCode = details.degreeCourseCode
)