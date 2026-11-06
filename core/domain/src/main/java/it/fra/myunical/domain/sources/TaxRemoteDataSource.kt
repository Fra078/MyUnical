package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.taxes.StudentTaxes

interface TaxRemoteDataSource {

    /** Every (non cancelled) charge of the career [stuId]. Throws on network or HTTP errors. */
    suspend fun getStudentTaxes(stuId: Int): StudentTaxes
}
