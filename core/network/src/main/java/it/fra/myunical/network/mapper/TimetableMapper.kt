package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.schedule.ScheduleRoom
import it.fra.myunical.network.models.schedule.TimetableEntryDto
import java.time.LocalDate
import java.time.LocalTime

/** Returns `null` for entries that cannot be linked to a study plan activity. */
fun TimetableEntryDto.toDomainModel(requestedCdsYear: Int): ScheduleEvent? = ScheduleEvent(
    providerId = "",
    courseCode = courseCode ?: return null,
    title = courseName,
    date = LocalDate.parse(startDate),
    start = LocalTime.parse(startTime),
    end = LocalTime.parse(endTime),
    rooms = rooms.map { ScheduleRoom(name = it.name, building = it.building) },
    teachers = teachers,
    cdsYear = cdsYear ?: requestedCdsYear
)
