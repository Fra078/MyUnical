package it.fra.myunical.career.models

enum class ExamSortOption(val title: String, val subtitle: String) {
    CHRONOLOGICAL_DESC("Data di conseguimento", "Dal più recente al più vecchio"),
    GRADE_DESC("Voto conseguito", "Dal più alto al più basso"),
    ACADEMIC_YEAR_DESC("Anno accademico", "Dal più recente al più vecchio"),
    ALPHABETICAL_ASC("Ordine alfabetico", "Dalla A alla Z")
}