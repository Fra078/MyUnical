package it.fra.myunical.domain.model.news

/** Where a list of news comes from; each source has its own paged feed. */
sealed interface NewsSource {
    /** University news with images (home page of unical.it). */
    data object University : NewsSource

    /** University notices ("avvisi"): short items without image. */
    data object Notices : NewsSource

    /** News and notices published by the site of a department; [noticesOnly] keeps just the notices. */
    data class Department(val host: String, val noticesOnly: Boolean = false) : NewsSource
}
