package it.fra.myunical.taxes.models

import it.fra.myunical.domain.model.taxes.TaxStatus

data class TaxesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Set when the last load failed; previous data, if any, is still shown. */
    val hasError: Boolean = false,
    val overview: TaxOverview? = null,
    val unpaid: List<InvoiceUiModel> = emptyList(),
    val paidGroups: List<PaidYearGroup> = emptyList(),
    val expandedIds: Set<String> = emptySet()
) {
    val hasContent: Boolean get() = overview != null
    val isEmpty: Boolean get() = unpaid.isEmpty() && paidGroups.isEmpty()
}

enum class OverviewTone { OK, WARNING, ALERT }

data class TaxOverview(
    val tone: OverviewTone,
    val title: String,
    val amount: String?,
    val description: String
)

data class InvoiceUiModel(
    val id: String,
    val title: String,
    /** e.g. "Rata 1 di 4" or "Rata unica". */
    val installment: String?,
    /** e.g. "A.A. 2026/2027". */
    val academicYear: String?,
    val amount: String,
    val status: TaxStatus,
    /** "Scade tra 5 giorni", "Scaduta il 30/11/2026", "Pagata il 16/07/2026"... */
    val dateLabel: String?,
    val noticeCode: String?,
    val items: List<InvoiceItemUiModel>
) {
    val subtitle: String get() = listOfNotNull(installment, academicYear).joinToString(" · ")
}

data class InvoiceItemUiModel(
    val description: String,
    val amount: String
)

data class PaidYearGroup(
    val label: String,
    val invoices: List<InvoiceUiModel>
)

sealed interface TaxesIntent {
    data object Refresh : TaxesIntent
    data class ToggleExpanded(val id: String) : TaxesIntent
}
