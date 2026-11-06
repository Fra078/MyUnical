package it.fra.myunical.career.models

data class ExamRecord(
    val id: String,
    val title: String,
    val scientificSector: String,
    val credits: Int,
    val grade: String?,
    val isPassed: Boolean,
    val date: String?,
    val academicYear: String
)