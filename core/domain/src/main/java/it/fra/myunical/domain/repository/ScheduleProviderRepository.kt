package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.schedule.ScheduleProvider
import kotlinx.coroutines.flow.Flow

interface ScheduleProviderRepository {
    suspend fun addProvider(item: ScheduleProvider)
    fun getProviders(): Flow<List<ScheduleProvider>>
}