package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.sources.UnicalStorageDataSource

/** Degree programs offered this academic year, sorted by name. */
class GetDegreeProgramsUseCase(
    private val storageDataSource: UnicalStorageDataSource
) {
    suspend operator fun invoke(): List<DegreeProgram> = storageDataSource.getAllDegreeCourses().sortedBy { it.name }
}

/** Study paths (curricula) of a degree program, with their courses by year. */
class GetStudyPathsUseCase(
    private val storageDataSource: UnicalStorageDataSource
) {
    suspend operator fun invoke(regulationId: Int): List<StudyPath> = storageDataSource.getStudyPlansForCds(regulationId)
}
