package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.taxes.StudentTaxes
import it.fra.myunical.domain.model.taxes.TaxInvoice
import it.fra.myunical.domain.model.taxes.TaxItem
import it.fra.myunical.domain.model.taxes.TaxSemaphore
import it.fra.myunical.network.models.taxes.StudentChargeDto
fun List<StudentChargeDto>.toStudentTaxes(stuId: Int): StudentTaxes {
    val first = firstOrNull()
    val visible = filter {
        it.annullataFlg != 1 && (it.fattAnnullata ?: 0L) == 0L && it.visWebFlg != 0
    }
    return StudentTaxes(
        stuId = stuId,
        semaphore = first?.semaforo.toSemaphore(),
        amountDue = first?.dovuto,
        // Rows are fee items: an invoice spans several rows, uninvoiced charges stand alone
        invoices = visible
            .groupBy { row -> row.fattId?.let { "F$it" } ?: "A${row.tassaId}-${row.rataId}-${row.aaId}" }
            .map { (id, rows) -> rows.toInvoice(id) }
    )
}

private fun List<StudentChargeDto>.toInvoice(id: String): TaxInvoice {
    val head = first()
    val isPaid = head.pagatoFlg == 1
    val items = map { row ->
        TaxItem(
            description = row.voceDes.clean() ?: row.tassaDes.clean() ?: "Voce",
            amount = row.importoVoce ?: 0.0
        )
    }
    return TaxInvoice(
        id = id,
        invoiceId = head.fattId,
        academicYear = head.aaId,
        description = head.tassaDes.clean() ?: head.desMav1.clean() ?: "Addebito",
        installment = head.rataDes.clean(),
        dueDate = (head.scadFattura.clean() ?: head.scadenzaAddebito.clean()).toEsse3Date(),
        amount = head.importoFattura ?: items.sumOf(TaxItem::amount),
        isPaid = isPaid,
        paidAmount = head.importoPag,
        paymentDate = head.dataPagamento.clean().toEsse3Date().takeIf { isPaid },
        pagoPaNoticeCode = head.codiceAvviso.clean(),
        items = items
    )
}

private fun String?.toSemaphore() = when (this?.uppercase()) {
    "VERDE" -> TaxSemaphore.GREEN
    "GIALLO" -> TaxSemaphore.YELLOW
    "ROSSO" -> TaxSemaphore.RED
    else -> TaxSemaphore.UNKNOWN
}
