package it.fra.myunical.today.models

data class StudentHeaderInfo(
    val firstName: String,
    val matricola: String,
    val avatarUrl: String? = null
)