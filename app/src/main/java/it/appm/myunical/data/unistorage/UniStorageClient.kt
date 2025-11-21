package it.appm.myunical.data.unistorage


import android.util.Log
import it.appm.myunical.model.Evento
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

class UniStorageClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://storage.portale.unical.it/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val client = retrofit.create(UniStorageService::class.java)

    suspend fun getSchedule(cdsID: String, annoAccademico: Int, annoCds: Int, year: Int, month: Int) : Result<List<Evento>>{
        try {
            val response = client.getSchedule(
                cdsId = cdsID,
                academicYear = annoAccademico,
                annoCds = annoCds,
                year = year,
                month = month
            )
            if (!response.isSuccessful) return Result.failure(Exception("No data found"))
            val body = response.body()!!
            val list = body.mapIndexed { i, it ->
                println(it)
                val (year, month, day) =
                    (it["dataInizio"] as String).split("-").map(String::toInt)

                val inizioOffset =
                    with((it["orarioInizio"] as String).split(':')){
                        this[0].toInt() * 60 + this[1].toInt()
                    }
                val fineOffset =
                    with((it["orarioFine"] as String).split(':')){
                        this[0].toInt() * 60 + this[1].toInt()
                    }
                val title = it["insegnamento"] as String
                val location = ((it["aule"] as? List<Map<String, Any>>)?.firstOrNull()?.get("nome") as? String) ?: ""
                Evento(i + year*12+month, title, year, month, day, inizioOffset, fineOffset, location)
            }
            println(list)
            return Result.success(list)
        } catch (exc: Exception){
            Log.e("ScheduleExc", exc.toString())
            return Result.failure(exc)
        }
    }

    interface UniStorageService {
        @GET("ricerca/cds-websites/{cdsId}/timetable/")
        @Headers("accept: application/json")
        suspend fun getSchedule(
            @Path("cdsId") cdsId: String,
            @Query("academic_year") academicYear: Int,
            @Query("year") annoCds: Int,
            @Query("date_year") year: Int,
            @Query("date_month") month: Int,
            @Query("lang") lang: String = "it",

        ): Response<List<Map<String, Any>>>
    }

    }