package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.Course

interface CinecaRemoteDataSource {

    fun getUserPhotoEndpoint(persId: Int) : String

    suspend fun fetchStudyPlan(matId: Int): Result<List<Course>>
}