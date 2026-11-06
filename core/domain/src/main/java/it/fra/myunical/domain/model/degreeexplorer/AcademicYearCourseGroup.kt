package it.fra.myunical.domain.model.degreeexplorer

data class AcademicYearCourseGroup(
    val yearNumber: Int,
    val courses: List<SubjectCourse>
)