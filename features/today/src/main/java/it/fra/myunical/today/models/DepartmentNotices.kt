package it.fra.myunical.today.models

/** Latest notices of the student's department, shown under the news. */
data class DepartmentNotices(
    /** Short name of the department, e.g. "DESF". */
    val department: String,
    val items: List<NoticeUiModel>
)
