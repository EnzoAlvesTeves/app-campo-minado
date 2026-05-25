package com.enzo.campominado

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("get_scores.php")
    fun getScores(): Call<List<Score>>

    @POST("save_score.php")
    fun saveScore(@Body score: Score): Call<Map<String, String>>
}