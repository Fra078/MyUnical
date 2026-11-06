package it.fra.myunical.domain.model.degreeexplorer

data class StudyPath(
    val id: Int,
    val name: String,
    val academicYears: List<AcademicYearCourseGroup>
)