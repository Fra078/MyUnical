package it.appm.myunical.data.news


import it.appm.myunical.model.News
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NewsClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.unical.it/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val client = retrofit.create(NewsService::class.java)

    suspend fun getNews() : Result<List<News>>{
        try {
            val response = client.getNews()
            if (!response.isSuccessful) return Result.failure(Exception("No data found"))
            val body = response.body()!!
            val results = body["results"] as List<Map<String, Any>>
            val list = results.map {
                val info = it["publication"] as Map<String, Any>
                val title = info["full_name"] as String
                val imageData = info["preview_image"] as Map<String, Any>
                val dt = ZonedDateTime.parse(it["created"] as String, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val date = dt.toInstant().toEpochMilli()
                val id = (it["id"] as Double).toInt()
                val path = "https://www.unical.it" + imageData["file"]
                val url = "https://www.unical.it" + it["path"]
                News(id, title, path, date, url)
            }
            return Result.success(list)
        } catch (exc: Exception){
            return Result.failure(exc)
        }
    }

    interface NewsService {
        @GET("news/by-context/1")
        @Headers("accept: application/json")
        suspend fun getNews(
        ): Response<Map<String, Any>>
    }

    }