package it.appm.myunical.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["appelloId", "disciplinaId"])
data class InfoAppelloEntity(
    val appelloId: Int,
    val disciplinaId: Int,
    @ColumnInfo val titolo: String,
    @ColumnInfo val disciplinaCod: String,
    @ColumnInfo val aula: String,
    @ColumnInfo val description: String,
    @ColumnInfo val dataOraEsa: String,
){
    constructor(infoAppello: InfoAppello) :
            this(appelloId = infoAppello.appelloId,
                disciplinaId = infoAppello.disciplinaId,
                titolo = infoAppello.titolo,
                disciplinaCod = infoAppello.disciplinaCod,
                aula = infoAppello.turni[0].aula,
                description = infoAppello.turni[0].description,
                dataOraEsa = infoAppello.turni[0].dataOraEsa
            )
}
