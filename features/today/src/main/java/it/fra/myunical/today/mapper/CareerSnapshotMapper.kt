package it.fra.myunical.today.mapper

import it.fra.myunical.domain.model.career.CareerData
import it.fra.myunical.today.models.CareerSnapshotState
import java.util.Locale

fun Result<CareerData>.toSnapshotState(): CareerSnapshotState = fold(
    onSuccess = { data ->
        val overview = data.overview
        CareerSnapshotState.Available(
            earnedCredits = overview.earnedCredits,
            requiredCredits = overview.requiredCredits,
            progress = overview.progress,
            weightedAverage = overview.weightedAverage?.let { String.format(Locale.ITALY, "%.2f", it) },
            openSessions = data.openSessions.size
        )
    },
    onFailure = { CareerSnapshotState.Unavailable }
)
