package it.fra.myunical.domain.model.degreeexplorer

data class DegreeProgram(
    val cdsCode: String,
    val regulationId: Int,
    val name: String,
    val category: DegreeCategory,
    val courseType: String = "",
    val area: String = "",
    val departmentName: String = "",
    val durationYears: Int = 0,
    /** Absolute URL of the didactic regulation PDF, when published for the requested year. */
    val regulationUrl: String? = null,
    /** Code of the department running the course, matching [Department.code]. */
    val departmentCode: String = ""
)
