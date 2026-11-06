package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.taxes.StudentTaxes
import it.fra.myunical.domain.sources.TaxRemoteDataSource
import it.fra.myunical.network.mapper.toStudentTaxes
import it.fra.myunical.network.models.taxes.StudentChargeDto

class TaxRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : TaxRemoteDataSource {

    override suspend fun getStudentTaxes(stuId: Int): StudentTaxes {
        val rows = buildList {
            var start = 0
            do {
                val response = httpClient.get("tasse-service-v1/addebiti-studente") {
                    parameter("stuId", stuId)
                    parameter("start", start)
                    parameter("limit", PAGE_SIZE)
                }
                if (response.status != HttpStatusCode.OK)
                    throw IllegalStateException("Errore nella richiesta: ${response.status}")
                val page = response.body<List<StudentChargeDto>>()
                addAll(page)
                start += PAGE_SIZE
            } while (page.size == PAGE_SIZE)
        }
        return rows.toStudentTaxes(stuId)
    }

    private companion object {
        // Maximum allowed by the API
        const val PAGE_SIZE = 100
    }
}
