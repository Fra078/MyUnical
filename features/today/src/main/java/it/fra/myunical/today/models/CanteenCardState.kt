package it.fra.myunical.today.models

sealed interface CanteenCardState {
    data object Loading : CanteenCardState

    /** No menu published for the coming days. */
    data object Unavailable : CanteenCardState

    data class Available(
        /** "Oggi a pranzo", "Domani a cena", "Lunedì a pranzo" */
        val title: String,
        /** First course of each line, e.g. "Pasta al pesto" */
        val highlights: List<String>,
        val dishCount: Int
    ) : CanteenCardState
}
