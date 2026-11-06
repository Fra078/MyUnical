package it.fra.myunical.today.mapper

import it.fra.myunical.domain.model.taxes.TaxSummary
import it.fra.myunical.today.models.TuitionState
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ShortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun Result<TaxSummary>.toTuitionState(): TuitionState = fold(
    onSuccess = { it.toTuitionState() },
    onFailure = { TuitionState.Unavailable }
)

fun TaxSummary.toTuitionState(): TuitionState {
    if (isUpToDate) return TuitionState.UpToDate
    val nextDue = unpaid.firstOrNull { it.dueDate != null }?.dueDate
    return TuitionState.Due(
        amount = NumberFormat.getCurrencyInstance(Locale.ITALY).format(outstandingAmount),
        dueLabel = nextDue?.let {
            if (it.isBefore(today)) "scaduta il ${it.format(ShortDateFormatter)}"
            else "scade il ${it.format(ShortDateFormatter)}"
        },
        pendingCount = unpaid.size,
        isOverdue = hasOverdue
    )
}
