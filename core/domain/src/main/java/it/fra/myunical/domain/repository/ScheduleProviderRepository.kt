package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.schedule.ScheduleProvider
import kotlinx.coroutines.flow.Flow

interface ScheduleProviderRepository {
    fun getProviders(): Flow<List<ScheduleProvider>>
    suspend fun getProvider(id: String): ScheduleProvider?
    suspend fun upsertProvider(item: ScheduleProvider)
    suspend fun removeProvider(id: String)

    /** True when the user removed the automatic schedule of [careerMatId], so it must not be recreated. */
    suspend fun isCareerScheduleDismissed(careerMatId: Int): Boolean
}
