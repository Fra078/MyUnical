package it.fra.myunical.domain.model.degreeexplorer

/** A university department and the host of its website (the news source of its students). */
data class Department(
    val code: String,
    val shortName: String,
    val name: String,
    /** Host name of the department site, e.g. "desf.unical.it". */
    val host: String
)
