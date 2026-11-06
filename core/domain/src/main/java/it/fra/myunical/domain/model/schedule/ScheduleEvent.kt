package it.fra.myunical.domain.model.schedule

import java.time.LocalDate
import java.time.LocalTime

data class ScheduleEvent(
    val providerId: String,
    val courseCode: String,
    val title: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val rooms: List<ScheduleRoom>,
    val teachers: List<String>,
    val cdsYear: Int
)

data class ScheduleRoom(
    val name: String,
    val building: String?
)
