package it.fra.myunical.domain.model.schedule

import java.time.LocalDate

/**
 * Unical academic years start in September and are identified by their starting calendar year
 * (e.g. March 2027 belongs to academic year 2026).
 */
object AcademicYear {
    private const val FIRST_MONTH = 9

    fun of(year: Int, month: Int): Int = if (month >= FIRST_MONTH) year else year - 1

    fun current(): Int = LocalDate.now().let { of(it.year, it.monthValue) }
}
