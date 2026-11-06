package it.fra.myunical.today.models

data class TuitionStatus(
    val isUpToDate: Boolean,
    val pendingAmount: Double? = null,
    val dueDate: String? = null,
    val installmentDescription: String? = null
)