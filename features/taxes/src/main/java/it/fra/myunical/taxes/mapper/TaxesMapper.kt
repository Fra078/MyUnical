package it.fra.myunical.taxes.mapper

import it.fra.myunical.domain.model.taxes.TaxInvoice
import it.fra.myunical.domain.model.taxes.TaxSemaphore
import it.fra.myunical.domain.model.taxes.TaxStatus
import it.fra.myunical.domain.model.taxes.TaxSummary
import it.fra.myunical.taxes.models.InvoiceItemUiModel
import it.fra.myunical.taxes.models.InvoiceUiModel
import it.fra.myunical.taxes.models.OverviewTone
import it.fra.myunical.taxes.models.PaidYearGroup
import it.fra.myunical.taxes.models.TaxOverview
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val ItalianLocale: Locale = Locale.ITALY
private val DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", ItalianLocale)

fun Double.formatEuro(): String = NumberFormat.getCurrencyInstance(ItalianLocale).format(this)

/** Esse3 identifies the academic year by its starting year: 2026 -> "2026/2027". */
fun Int.toAcademicYearLabel(): String = "$this/${this + 1}"

fun TaxSummary.toOverview(): TaxOverview {
    val nextDue = unpaid.firstOrNull { it.dueDate != null }?.dueDate
    return when {
        isUpToDate -> TaxOverview(
            tone = OverviewTone.OK,
            title = "Sei in regola",
            amount = null,
            description = "Nessun pagamento in sospeso."
        )
        hasOverdue -> TaxOverview(
            tone = OverviewTone.ALERT,
            title = "Pagamenti scaduti",
            amount = outstandingAmount.formatEuro(),
            description = "Regolarizza al più presto: potrebbero essere applicate more " +
                "e bloccate alcune funzioni della carriera."
        )
        else -> TaxOverview(
            tone = OverviewTone.WARNING,
            title = "Da pagare",
            amount = outstandingAmount.formatEuro(),
            description = buildString {
                append(if (unpaid.size == 1) "1 addebito in attesa" else "${unpaid.size} addebiti in attesa")
                if (nextDue != null) append(" · prossima scadenza ${nextDue.format(DateFormatter)}")
                if (semaphore == TaxSemaphore.YELLOW) append(". Potrebbe essere applicata una mora.")
            }
        )
    }
}

fun TaxSummary.toUnpaidUiModels(): List<InvoiceUiModel> = unpaid.map { it.toUiModel(today) }

fun TaxSummary.toPaidGroups(): List<PaidYearGroup> = paid
    .groupBy { it.academicYear }
    .map { (year, invoices) ->
        PaidYearGroup(
            label = year?.let { "A.A. ${it.toAcademicYearLabel()}" } ?: "Altri addebiti",
            invoices = invoices.map { it.toUiModel(today) }
        )
    }

fun TaxInvoice.toUiModel(today: LocalDate): InvoiceUiModel {
    val status = status(today)
    return InvoiceUiModel(
        id = id,
        title = description.toDisplayName(),
        installment = installment?.let { if (it.first().isDigit()) "Rata $it" else it },
        academicYear = academicYear?.let { "A.A. ${it.toAcademicYearLabel()}" },
        amount = (if (status == TaxStatus.PAID) paidAmount ?: amount else outstandingAmount).formatEuro(),
        status = status,
        dateLabel = dateLabel(status, today),
        noticeCode = pagoPaNoticeCode.takeIf { status != TaxStatus.PAID },
        items = items.map { InvoiceItemUiModel(it.description.toDisplayName(), it.amount.formatEuro()) }
    )
}

private fun TaxInvoice.dateLabel(status: TaxStatus, today: LocalDate): String? = when (status) {
    TaxStatus.PAID -> paymentDate?.let { "Pagata il ${it.format(DateFormatter)}" } ?: "Pagata"
    TaxStatus.OVERDUE -> dueDate?.let { "Scaduta il ${it.format(DateFormatter)}" }
    TaxStatus.DUE -> dueDate?.let {
        when (val days = ChronoUnit.DAYS.between(today, it)) {
            0L -> "Scade oggi"
            1L -> "Scade domani"
            in 2L..30L -> "Scade tra $days giorni"
            else -> "Scade il ${it.format(DateFormatter)}"
        }
    } ?: "Nessuna scadenza"
}

/** Esse3 descriptions are sometimes all caps: "TASSA DI RILASCIO PERGAMENA" -> "Tassa di rilascio pergamena". */
private fun String.toDisplayName(): String =
    if (any { it.isLowerCase() }) this
    else lowercase(ItalianLocale).replaceFirstChar { it.titlecase(ItalianLocale) }
