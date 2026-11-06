package it.fra.myunical.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferenceRepository {

    suspend fun setCareerId(value: Int)

    fun getCareerId(): Flow<Int>

}