package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath

interface UnicalStorageDataSource {
    suspend fun getAllDegreeCourses(): List<DegreeProgram>
    suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath>
}