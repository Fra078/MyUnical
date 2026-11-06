package it.fra.myunical.career.models

data class CareerMetrics(
    val earnedCredits: Int,
    val totalCredits: Int,
    val weightedAverage: Double,
    val arithmeticAverage: Double,
    val graduationBaseGrade: Double
)
