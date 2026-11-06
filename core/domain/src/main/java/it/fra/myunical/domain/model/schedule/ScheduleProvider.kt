package it.fra.myunical.domain.model.schedule

data class ScheduleProvider(
    val cdsId: String,
    val name: String,
    val academicYear: Int,
    val cdsYear: Int
)
