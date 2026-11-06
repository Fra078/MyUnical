package it.fra.myunical.network.mapper

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Esse3DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val Esse3TimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

/** Esse3 strings are often empty or the literal "null" instead of being absent. */
internal fun String?.clean(): String? =
    this?.trim()?.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }

/** Parses `dd/MM/yyyy` or `dd/MM/yyyy HH:mm:ss`. */
internal fun String?.toEsse3Date(): LocalDate? = clean()?.take(10)?.let {
    runCatching { LocalDate.parse(it, Esse3DateFormatter) }.getOrNull()
}

/** Time part of `dd/MM/yyyy HH:mm:ss` (Esse3 uses 01/01/1900 as date for times). */
internal fun String?.toEsse3Time(): LocalTime? = clean()?.takeIf { it.length >= 19 }?.substring(11, 19)?.let {
    runCatching { LocalTime.parse(it, Esse3TimeFormatter) }.getOrNull()
}
