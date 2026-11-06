package it.fra.myunical.datastore.mapper

import it.fra.myunical.datastore.models.ScheduleProviderDataModel
import it.fra.myunical.datastore.models.TrackedCourseDataModel
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse

fun ScheduleProvider.toDataModel() = ScheduleProviderDataModel(
    id = id,
    displayName = displayName,
    cdsCode = cdsCode,
    regulationId = regulationId,
    degreeName = degreeName,
    studyPathId = studyPathId,
    studyPathName = studyPathName,
    courses = courses.map(TrackedCourse::toDataModel)
)

fun ScheduleProviderDataModel.toDomainModel() = ScheduleProvider(
    id = id,
    displayName = displayName,
    cdsCode = cdsCode,
    regulationId = regulationId,
    degreeName = degreeName,
    studyPathId = studyPathId,
    studyPathName = studyPathName,
    courses = courses.map(TrackedCourseDataModel::toDomainModel)
)

fun TrackedCourse.toDataModel() = TrackedCourseDataModel(
    code = code,
    name = name,
    yearNumber = yearNumber,
    moduleCodes = moduleCodes
)

fun TrackedCourseDataModel.toDomainModel() = TrackedCourse(
    code = code,
    name = name,
    yearNumber = yearNumber,
    moduleCodes = moduleCodes
)
