package it.fra.myunical.career.models

enum class ExamSortOption(val title: String, val subtitle: String) {
    COURSE_YEAR("Anno di corso", "Raggruppati per anno, come nel piano di studi"),
    DATE_DESC("Data di superamento", "Dal più recente al più vecchio"),
    GRADE_DESC("Voto", "Dal più alto al più basso"),
    ALPHABETICAL("Ordine alfabetico", "Dalla A alla Z")
}
