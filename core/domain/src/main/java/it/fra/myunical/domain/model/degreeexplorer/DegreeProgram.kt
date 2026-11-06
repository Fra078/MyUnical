package it.fra.myunical.domain.model.degreeexplorer

data class DegreeProgram(
    val id: String,
    val regulationId: Int,
    val name: String,
    val category: DegreeCategory
)